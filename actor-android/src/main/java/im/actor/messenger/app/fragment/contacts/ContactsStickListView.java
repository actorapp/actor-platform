package im.actor.messenger.app.fragment.contacts;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.view.View;
import android.widget.AbsListView;
import android.widget.HeaderViewListAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;

import im.actor.messenger.R;
import im.actor.messenger.app.view.Fonts;
import im.actor.messenger.util.Screen;

/**
 * Created by ex3ndr on 26.12.14.
 */
public class ContactsStickListView extends ListView {

    public interface StickAdapter {
        public String getItemHeader(int index);
    }

    private OnScrollListener listener;

    private TextPaint HEADER_PAINT = new TextPaint();
    private Paint HEADER_BG = new Paint();

    private String visibleHeader = null;
    private float headerMeasure;

    private String visibleHeaderNext = null;
    private float headerMeasureNext;

    private float HEADER_TEXT_HEIGHT;
    private float HEADER_HEIGHT;
    private float HEADER_TOP;
    private float HEADER_LEFT_PADDING;
    private float HEADER_WIDTH;

    private int offset;

    public ContactsStickListView(Context context) {
        super(context);
        init();
    }

    public ContactsStickListView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public ContactsStickListView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    private void init() {
        super.setOnScrollListener(new ScrollListener());

        HEADER_PAINT.setTypeface(Fonts.medium());
        HEADER_PAINT.setColor(getResources().getColor(R.color.primary));
        HEADER_PAINT.setTextSize(Screen.sp(18));
        HEADER_PAINT.setAntiAlias(true);

        HEADER_BG.setColor(getResources().getColor(R.color.bg_light));
        HEADER_BG.setStyle(Paint.Style.FILL);

        HEADER_HEIGHT = Screen.dp(64);

        HEADER_LEFT_PADDING = Screen.dp(6);
        HEADER_WIDTH = Screen.dp(40);

        HEADER_TEXT_HEIGHT = HEADER_PAINT.descent() - HEADER_PAINT.ascent();
        HEADER_TOP = (HEADER_HEIGHT - HEADER_TEXT_HEIGHT) / 2.0f - HEADER_PAINT.ascent();
    }

    @Override
    public void setOnScrollListener(OnScrollListener l) {
        this.listener = l;
    }

    @Override
    public void setAdapter(ListAdapter adapter) {
        if (!(adapter instanceof StickAdapter)) {
            throw new IllegalArgumentException("Adapter must implement StickAdapter");
        }
        super.setAdapter(adapter);
    }

    @Override
    public void draw(Canvas canvas) {
        super.draw(canvas);

        if (visibleHeader != null) {
            canvas.drawRect(HEADER_LEFT_PADDING, offset, HEADER_WIDTH, offset + HEADER_HEIGHT, HEADER_BG);
            canvas.drawText(visibleHeader, HEADER_LEFT_PADDING + (HEADER_WIDTH - headerMeasure) / 2, offset + HEADER_TOP, HEADER_PAINT);
        }

        if (visibleHeaderNext != null) {
            canvas.drawRect(HEADER_LEFT_PADDING, offset + HEADER_HEIGHT, HEADER_WIDTH, offset + HEADER_HEIGHT + HEADER_HEIGHT, HEADER_BG);
            canvas.drawText(visibleHeaderNext, HEADER_LEFT_PADDING + (HEADER_WIDTH - headerMeasureNext) / 2.0f, offset + HEADER_TOP + HEADER_HEIGHT, HEADER_PAINT);
        }
    }

    private class ScrollListener implements OnScrollListener {

        @Override
        public void onScrollStateChanged(AbsListView absListView, int i) {
            if (listener != null) {
                listener.onScrollStateChanged(absListView, i);
            }
        }

        @Override
        public void onScroll(AbsListView absListView, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
            if (listener != null) {
                listener.onScroll(absListView, firstVisibleItem, visibleItemCount, totalItemCount);
            }

            ListAdapter adapter;
            if (getAdapter() instanceof HeaderViewListAdapter) {
                adapter = ((HeaderViewListAdapter) getAdapter()).getWrappedAdapter();
            } else {
                adapter = getAdapter();
            }

            if (firstVisibleItem == 0) {
                visibleHeader = null;
                visibleHeaderNext = null;

                View view = getChildAt(1);
                if (view != null) {
                    offset = view.getTop();
                    if (getAdapter().getCount() > 0) {
                        visibleHeader = ((StickAdapter) adapter).getItemHeader(0);
                        headerMeasure = HEADER_PAINT.measureText(visibleHeader);
                    }
                }
            } else {
                int realFirstVisibleItem = firstVisibleItem - getHeaderViewsCount();
                if (realFirstVisibleItem >= 0 && realFirstVisibleItem < adapter.getCount()) {
                    boolean isSameHeader = true;
                    String header = ((StickAdapter) adapter).getItemHeader(realFirstVisibleItem);
                    String nextHeader = null;

                    if (realFirstVisibleItem >= 0 && realFirstVisibleItem + 1 < adapter.getCount()) {
                        nextHeader = ((StickAdapter) adapter).getItemHeader(realFirstVisibleItem + 1);
                        isSameHeader = nextHeader.equals(header);
                    }

                    if (isSameHeader) {
                        offset = 0;
                        visibleHeaderNext = null;
                    } else {
                        View view = getChildAt(0);
                        if (view != null) {
                            offset = view.getTop();
                        }

                        visibleHeaderNext = nextHeader;
                        headerMeasureNext = HEADER_PAINT.measureText(visibleHeaderNext);
                    }

                    visibleHeader = header;
                    headerMeasure = HEADER_PAINT.measureText(visibleHeader);
                } else {
                    visibleHeader = null;
                    visibleHeaderNext = null;
                }
            }
        }
    }
}
