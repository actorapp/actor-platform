package im.actor.sdk.view.emoji.keyboard.emoji;

import android.content.Context;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.PopupWindow;

import im.actor.sdk.view.emoji.EmojiData;

/**
 * Created by 98379720172 on 03/01/17.
 */

public class EmojiView extends FrameLayout {

    public interface Listener {
        boolean onBackspace();
        void onEmojiSelected(String emoji);
        void onStickersSettingsClick();
        void onGifTab(boolean opened);
        void onClearEmojiRecent();
    }


    private class ImageViewEmoji extends ImageView {

        private boolean touched;
        private float lastX;
        private float lastY;
        private float touchedX;
        private float touchedY;

        public ImageViewEmoji(Context context) {
            super(context);
            setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    sendEmoji(null);
                }
            });
            setOnLongClickListener(new OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    String code = (String) view.getTag();
                    if (EmojiData.emojiColoredMap.containsKey(code)) {
                        touched = true;
                        touchedX = lastX;
                        touchedY = lastY;

                        String color = emojiColor.get(code);
                        if (color != null) {
                            switch (color) {
                                case "\uD83C\uDFFB":
                                    pickerView.setSelection(1);
                                    break;
                                case "\uD83C\uDFFC":
                                    pickerView.setSelection(2);
                                    break;
                                case "\uD83C\uDFFD":
                                    pickerView.setSelection(3);
                                    break;
                                case "\uD83C\uDFFE":
                                    pickerView.setSelection(4);
                                    break;
                                case "\uD83C\uDFFF":
                                    pickerView.setSelection(5);
                                    break;
                            }
                        } else {
                            pickerView.setSelection(0);
                        }
                        view.getLocationOnScreen(location);
                        int x = emojiSize * pickerView.getSelection() + AndroidUtilities.dp(4 * pickerView.getSelection() - (AndroidUtilities.isTablet() ? 5 : 1));
                        if (location[0] - x < AndroidUtilities.dp(5)) {
                            x += (location[0] - x) - AndroidUtilities.dp(5);
                        } else if (location[0] - x + popupWidth > AndroidUtilities.displaySize.x - AndroidUtilities.dp(5)) {
                            x += (location[0] - x + popupWidth) - (AndroidUtilities.displaySize.x - AndroidUtilities.dp(5));
                        }
                        int xOffset = -x;
                        int yOffset = view.getTop() < 0 ? view.getTop() : 0;

                        pickerView.setEmoji(code, AndroidUtilities.dp(AndroidUtilities.isTablet() ? 30 : 22) - xOffset + (int) AndroidUtilities.dpf2(0.5f));

                        pickerViewPopup.setFocusable(true);
                        pickerViewPopup.showAsDropDown(view, xOffset, -view.getMeasuredHeight() - popupHeight + (view.getMeasuredHeight() - emojiSize) / 2 - yOffset);
                        view.getParent().requestDisallowInterceptTouchEvent(true);
                        return true;
                    } else if (pager.getCurrentItem() == 0) {
                        listener.onClearEmojiRecent();
                    }
                    return false;
                }
            });
            setBackgroundResource(R.drawable.list_selector);
            setScaleType(ImageView.ScaleType.CENTER);
        }

        private void sendEmoji(String override) {
            String code = override != null ? override : (String) getTag();
            if (override == null) {
                if (pager.getCurrentItem() != 0) {
                    String color = emojiColor.get(code);
                    if (color != null) {
                        code = addColorToCode(code, color);
                    }
                }
                Integer count = emojiUseHistory.get(code);
                if (count == null) {
                    count = 0;
                }
                if (count == 0 && emojiUseHistory.size() > 50) {
                    for (int a = recentEmoji.size() - 1; a >= 0; a--) {
                        String emoji = recentEmoji.get(a);
                        emojiUseHistory.remove(emoji);
                        recentEmoji.remove(a);
                        if (emojiUseHistory.size() <= 50) {
                            break;
                        }
                    }
                }
                emojiUseHistory.put(code, ++count);
                if (pager.getCurrentItem() != 0) {
                    sortEmoji();
                }
                saveRecentEmoji();
                adapters.get(0).notifyDataSetChanged();
                if (listener != null) {
                    listener.onEmojiSelected(Emoji.fixEmoji(code));
                }
            } else {
                if (listener != null) {
                    listener.onEmojiSelected(Emoji.fixEmoji(override));
                }
            }
        }

        @Override
        public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
            setMeasuredDimension(View.MeasureSpec.getSize(widthMeasureSpec), View.MeasureSpec.getSize(widthMeasureSpec));
        }

        @Override
        public boolean onTouchEvent(MotionEvent event) {
            if (touched) {
                if (event.getAction() == MotionEvent.ACTION_UP || event.getAction() == MotionEvent.ACTION_CANCEL) {
                    if (pickerViewPopup != null && pickerViewPopup.isShowing()) {
                        pickerViewPopup.dismiss();

                        String color = null;
                        switch (pickerView.getSelection()) {
                            case 1:
                                color = "\uD83C\uDFFB";
                                break;
                            case 2:
                                color = "\uD83C\uDFFC";
                                break;
                            case 3:
                                color = "\uD83C\uDFFD";
                                break;
                            case 4:
                                color = "\uD83C\uDFFE";
                                break;
                            case 5:
                                color = "\uD83C\uDFFF";
                                break;
                        }
                        String code = (String) getTag();
                        if (pager.getCurrentItem() != 0) {
                            if (color != null) {
                                emojiColor.put(code, color);
                                code = addColorToCode(code, color);
                            } else {
                                emojiColor.remove(code);
                            }
                            setImageDrawable(Emoji.getEmojiBigDrawable(code));
                            sendEmoji(null);
                            saveEmojiColors();
                        } else {
                            sendEmoji(code + (color != null ? color : ""));
                        }
                    }
                    touched = false;
                    touchedX = -10000;
                    touchedY = -10000;
                } else if (event.getAction() == MotionEvent.ACTION_MOVE) {
                    boolean ignore = false;
                    if (touchedX != -10000) {
                        if (Math.abs(touchedX - event.getX()) > AndroidUtilities.getPixelsInCM(0.2f, true) || Math.abs(touchedY - event.getY()) > AndroidUtilities.getPixelsInCM(0.2f, false)) {
                            touchedX = -10000;
                            touchedY = -10000;
                        } else {
                            ignore = true;
                        }
                    }
                    if (!ignore) {
                        getLocationOnScreen(location);
                        float x = location[0] + event.getX();
                        pickerView.getLocationOnScreen(location);
                        x -= location[0] + AndroidUtilities.dp(3);
                        int position = (int) (x / (emojiSize + AndroidUtilities.dp(4)));
                        if (position < 0) {
                            position = 0;
                        } else if (position > 5) {
                            position = 5;
                        }
                        pickerView.setSelection(position);
                    }
                }
            }
            lastX = event.getX();
            lastY = event.getY();
            return super.onTouchEvent(event);
        }
    }



    private class EmojiPopupWindow extends PopupWindow {

        private ViewTreeObserver.OnScrollChangedListener mSuperScrollListener;
        private ViewTreeObserver mViewTreeObserver;

        public EmojiPopupWindow() {
            super();
            init();
        }

        public EmojiPopupWindow(Context context) {
            super(context);
            init();
        }

        public EmojiPopupWindow(int width, int height) {
            super(width, height);
            init();
        }

        public EmojiPopupWindow(View contentView) {
            super(contentView);
            init();
        }

        public EmojiPopupWindow(View contentView, int width, int height, boolean focusable) {
            super(contentView, width, height, focusable);
            init();
        }

        public EmojiPopupWindow(View contentView, int width, int height) {
            super(contentView, width, height);
            init();
        }

        private void init() {
            if (superListenerField != null) {
                try {
                    mSuperScrollListener = (ViewTreeObserver.OnScrollChangedListener) superListenerField.get(this);
                    superListenerField.set(this, NOP);
                } catch (Exception e) {
                    mSuperScrollListener = null;
                }
            }
        }

        private void unregisterListener() {
            if (mSuperScrollListener != null && mViewTreeObserver != null) {
                if (mViewTreeObserver.isAlive()) {
                    mViewTreeObserver.removeOnScrollChangedListener(mSuperScrollListener);
                }
                mViewTreeObserver = null;
            }
        }

        private void registerListener(View anchor) {
            if (mSuperScrollListener != null) {
                ViewTreeObserver vto = (anchor.getWindowToken() != null) ? anchor.getViewTreeObserver() : null;
                if (vto != mViewTreeObserver) {
                    if (mViewTreeObserver != null && mViewTreeObserver.isAlive()) {
                        mViewTreeObserver.removeOnScrollChangedListener(mSuperScrollListener);
                    }
                    if ((mViewTreeObserver = vto) != null) {
                        vto.addOnScrollChangedListener(mSuperScrollListener);
                    }
                }
            }
        }

        @Override
        public void showAsDropDown(View anchor, int xoff, int yoff) {
            try {
                super.showAsDropDown(anchor, xoff, yoff);
                registerListener(anchor);
            } catch (Exception e) {
                FileLog.e("tmessages", e);
            }
        }

        @Override
        public void update(View anchor, int xoff, int yoff, int width, int height) {
            super.update(anchor, xoff, yoff, width, height);
            registerListener(anchor);
        }

        @Override
        public void update(View anchor, int width, int height) {
            super.update(anchor, width, height);
            registerListener(anchor);
        }

        @Override
        public void showAtLocation(View parent, int gravity, int x, int y) {
            super.showAtLocation(parent, gravity, x, y);
            unregisterListener();
        }

        @Override
        public void dismiss() {
            setFocusable(false);
            try {
                super.dismiss();
            } catch (Exception e) {
                //don't promt
            }
            unregisterListener();
        }
    }

}
