package im.actor.messenger.app.fragment.chat;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;
import android.widget.AbsListView;
import android.widget.ListView;
import im.actor.messenger.util.Logger;
import im.actor.messenger.util.VisibleViewItem;

/**
 * Created by ex3ndr on 10.09.14.
 */
public class ConversationListView extends ListView {

    private static final int ACTIVATE_DELTA = 1600;
    private static final int SWITCH_DELTA = 30;

    private static final int BORDER_GAP = 3;

    private static final long UI_TIMEOUT = 900;

    private static final int ACTIVATE = 0;
    private static final int DEACTIVATE = 1;

    private static int ACTIVATE_DELTA_DP;
    private static int SWITCH_DELTA_DP;

    private int oldHeight;
    private boolean isUiActivated = false;
    private boolean isScrollingUp = false;

    private int scrollDistance;

    private boolean isScrolledToEnd = true;

    private Handler handler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == ACTIVATE) {
                if (!isUiActivated) {
                    isUiActivated = true;
                    scrollDistance = 0;
                    if (exScrollListener != null) {
                        if (isScrollingUp) {
                            exScrollListener.onScrolledUp();
                        } else {
                            exScrollListener.onScrolledDown();
                        }
                    }
                }
            } else if (msg.what == DEACTIVATE) {
                if (isUiActivated) {
                    isUiActivated = false;
                    scrollDistance = 0;
                    if (exScrollListener != null) {
                        exScrollListener.onStoppedScroll();
                    }
                }
            }
        }
    };

    private ExScrollListener exScrollListener;

    public ConversationListView(Context context) {
        super(context);
        init();
    }

    public ConversationListView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public ConversationListView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    private void init() {
        setOnScrollListener(new ScrollListener());
        ACTIVATE_DELTA_DP = getPx(ACTIVATE_DELTA);
        SWITCH_DELTA_DP = getPx(SWITCH_DELTA);
    }

    public void setExScrollListener(ExScrollListener exScrollListener) {
        this.exScrollListener = exScrollListener;
    }

    private VisibleViewItem[] dump() {

        int childCount = getChildCount();

        int idCount = 0;
        int headerCount = 0;
        for (int i = 0; i < childCount; i++) {
            int index = getFirstVisiblePosition() + i;
            long id = getItemIdAtPosition(index);
            if (id > 0) {
                idCount++;
            } else {
                headerCount++;
            }
        }

        VisibleViewItem[] res = new VisibleViewItem[idCount];
        int resIndex = 0;
        for (int i = 0; i < childCount; i++) {
            View v = getChildAt(i);
            int index = getFirstVisiblePosition() + i;
            long id = getItemIdAtPosition(index);
            if (id > 0) {
                int top = ((v == null) ? 0 : v.getTop()) - getPaddingTop();
                res[resIndex++] = new VisibleViewItem(index + headerCount, top, id);
            }
        }

        return res;
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        VisibleViewItem[] items = null;
        if (changed) {
            items = dump();
        }
        super.onLayout(changed, l, t, r, b);
        if (changed) {
            final int changeDelta = (b - t) - oldHeight;
            if (changeDelta < 0 && items.length > 0) {
                final VisibleViewItem item = items[items.length - 1];
                setSelectionFromTop(item.getIndex(), item.getTop() + changeDelta);
                post(new Runnable() {
                    @Override
                    public void run() {
                        setSelectionFromTop(item.getIndex(), item.getTop() + changeDelta);
                    }
                });
            }
        }

        oldHeight = b - t;
    }


    protected int getPx(float dp) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, getResources().getDisplayMetrics());
    }

    private class ScrollListener implements OnScrollListener {

        private int state = SCROLL_STATE_IDLE;
        private int prevTrackItem = -1;
        private int lastTop = 0;


        @Override
        public void onScrollStateChanged(AbsListView view, int scrollState) {
            if (scrollState == SCROLL_STATE_FLING || scrollState == SCROLL_STATE_TOUCH_SCROLL) {
                handler.removeMessages(DEACTIVATE);
            }

            if (scrollState == SCROLL_STATE_IDLE) {
                handler.removeMessages(DEACTIVATE);
                handler.sendEmptyMessageDelayed(DEACTIVATE, UI_TIMEOUT);
            }

            state = scrollState;
        }

        @Override
        public void onScroll(AbsListView list, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
            int currentTrackItem = firstVisibleItem + visibleItemCount / 2;
            int lastVisibleItem = firstVisibleItem + visibleItemCount;

            if (prevTrackItem == -1 || prevTrackItem < firstVisibleItem || prevTrackItem > lastVisibleItem
                    || state == SCROLL_STATE_IDLE) {
                prevTrackItem = currentTrackItem;
                lastTop = 0;
                View view = getChildAt(currentTrackItem - firstVisibleItem + getHeaderViewsCount());
                if (view != null) {
                    lastTop = view.getTop();
                }
            } else {
                View prevTrack = getChildAt(prevTrackItem - firstVisibleItem + getHeaderViewsCount());
                if (prevTrack == null) {
                    return;
                }

                int topDelta = prevTrack.getTop() - lastTop;
                if (scrollDistance * topDelta < 0) {
                    scrollDistance = 0;
                } else {
                    scrollDistance += topDelta;
                }

                prevTrackItem = currentTrackItem;
                lastTop = 0;
                View track = getChildAt(currentTrackItem - firstVisibleItem + getHeaderViewsCount());
                if (track != null) {
                    lastTop = track.getTop();
                }
            }

            if (lastVisibleItem + BORDER_GAP >= totalItemCount) {
                if (!isScrolledToEnd) {
                    isScrolledToEnd = true;
                    if (exScrollListener != null) {
                        exScrollListener.onScrolledToEnd();
                    }
                }

            } else {
                if (isScrolledToEnd) {
                    isScrolledToEnd = false;
                    if (exScrollListener != null) {
                        exScrollListener.onScrolledFromEnd();
                    }
                }
            }

            if (isUiActivated) {
                if (firstVisibleItem <= BORDER_GAP) {
                    isUiActivated = false;
                    handler.removeMessages(DEACTIVATE);
                    handler.removeMessages(ACTIVATE);
                    if (exScrollListener != null) {
                        exScrollListener.onStoppedScroll();
                    }
                    scrollDistance = 0;
                } else if (lastVisibleItem + BORDER_GAP >= totalItemCount) {
                    isUiActivated = false;
                    handler.removeMessages(DEACTIVATE);
                    handler.removeMessages(ACTIVATE);
                    if (exScrollListener != null) {
                        exScrollListener.onStoppedScroll();
                        exScrollListener.onScrolledToEnd();
                    }
                    scrollDistance = 0;
                } else {
                    if (!isScrollingUp && scrollDistance > SWITCH_DELTA_DP) {
                        isScrollingUp = true;
                        if (exScrollListener != null) {
                            exScrollListener.onScrolledUp();
                        }
                    } else if (isScrollingUp && scrollDistance < -SWITCH_DELTA_DP) {
                        isScrollingUp = false;
                        if (exScrollListener != null) {
                            exScrollListener.onScrolledDown();
                        }
                    }
                }
            } else {
                if (firstVisibleItem <= BORDER_GAP) {

                } else if (lastVisibleItem + BORDER_GAP >= totalItemCount) {

                } else if (Math.abs(scrollDistance) > ACTIVATE_DELTA_DP) {
                    isScrollingUp = scrollDistance > 0;
                    handler.removeMessages(DEACTIVATE);
                    handler.removeMessages(ACTIVATE);
                    handler.sendEmptyMessage(ACTIVATE);
                }
            }
        }
    }
}
