/*
 * Copyright 2014 Ankush Sachdeva
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package im.actor.messenger.app.emoji.keyboard;

import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Rect;
import android.os.Build;
import android.os.Handler;
import android.os.SystemClock;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.view.WindowManager.LayoutParams;
import android.widget.FrameLayout;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;

import java.util.Arrays;
import java.util.List;

import im.actor.messenger.R;
import im.actor.messenger.app.util.Screen;
import im.actor.messenger.app.view.PagerSlidingTabStrip;

import static im.actor.messenger.app.Core.core;


public class EmojiKeyboardPopup extends PopupWindow
        implements EmojiRecentsListener {
    private PagerAdapter mEmojisAdapter;
    private int keyBoardHeight = 0;
    private Boolean pendingOpen = false;
    private Boolean isOpened = false;
    EmojiPackView.OnEmojiClickedListener onEmojiClickedListener;
    OnEmojiconBackspaceClickedListener onEmojiconBackspaceClickedListener;
    OnSoftKeyboardOpenCloseListener onSoftKeyboardOpenCloseListener;
    View rootView;
    Context mContext;



    public EmojiKeyboardPopup(View rootView, Context mContext) {
        super(mContext);
        this.mContext = mContext;
        this.rootView = rootView;
        setBackgroundDrawable(null);
        if (Build.VERSION.SDK_INT >= 21) {
            setElevation(0);
        }
        setContentView(createView());
        setSoftInputMode(LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
        //default size
        setSize((int) mContext.getResources().getDimension(R.dimen.keyboard_height), LayoutParams.MATCH_PARENT);
    }

    public void setOnSoftKeyboardOpenCloseListener(OnSoftKeyboardOpenCloseListener listener) {
        this.onSoftKeyboardOpenCloseListener = listener;
    }

    public void setOnEmojiClickedListener(EmojiPackView.OnEmojiClickedListener listener) {
        this.onEmojiClickedListener = listener;
    }

    public void setOnEmojiconBackspaceClickedListener(OnEmojiconBackspaceClickedListener listener) {
        this.onEmojiconBackspaceClickedListener = listener;

    }


    public void showAtBottom() {
        showAtLocation(rootView, Gravity.BOTTOM, 0, 0);
    }


    public void showAtBottomPending() {
        if (isSoftwareKeyBoardOpen())
            showAtBottom();
        else
            pendingOpen = true;
    }


    public Boolean isSoftwareKeyBoardOpen() {
        return isOpened;
    }


    @Override
    public void dismiss() {
        super.dismiss();
        EmojiRecentsController
                .getInstance(mContext).saveRecents();
    }


    public void setSizeForSoftKeyboard() {
        rootView.getViewTreeObserver().addOnGlobalLayoutListener(new OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                Rect r = new Rect();
                rootView.getWindowVisibleDisplayFrame(r);

                int screenHeight = rootView.getRootView()
                        .getHeight();
                int heightDifference = screenHeight
                        - (r.bottom - r.top);
                int resourceId = mContext.getResources()
                        .getIdentifier("status_bar_height",
                                "dimen", "android");
                if (resourceId > 0) {
                    heightDifference -= mContext.getResources()
                            .getDimensionPixelSize(resourceId);
                }
                int orientation = mContext.getResources().getConfiguration().orientation;

                int id = mContext.getResources().getIdentifier("config_showNavigationBar", "bool", "android");
                if (id > 0) {
                    if (mContext.getResources().getBoolean(id)) {
                        int navbarResId = mContext.getResources()
                                .getIdentifier(
                                        orientation == Configuration.ORIENTATION_PORTRAIT ? "navigation_bar_height" : "navigation_bar_height_landscape",
                                        "dimen", "android");
                        if (navbarResId > 0) {
                            heightDifference -= mContext.getResources()
                                    .getDimensionPixelSize(navbarResId);
                        }
                    }
                }
                if (heightDifference > 100) {
                    keyBoardHeight = heightDifference;
                    setSize(LayoutParams.MATCH_PARENT, keyBoardHeight);
                    if (isOpened == false) {
                        if (onSoftKeyboardOpenCloseListener != null)
                            onSoftKeyboardOpenCloseListener.onKeyboardOpen(keyBoardHeight);
                    }
                    isOpened = true;
                    if (pendingOpen) {
                        showAtBottom();
                        pendingOpen = false;
                    }
                } else {
                    isOpened = false;
                    if (onSoftKeyboardOpenCloseListener != null)
                        onSoftKeyboardOpenCloseListener.onKeyboardClose();
                }
            }
        });
    }

    public void setSize(int width, int height) {
        setWidth(width);
        setHeight(height);
    }

    private View createView() {
        View keyboardView = ((LayoutInflater) mContext.getSystemService(Activity.LAYOUT_INFLATER_SERVICE))
                .inflate(R.layout.emoji_keyboard, null, false);
        ViewPager emojiPager = (ViewPager) keyboardView.findViewById(R.id.emoji_pager);
        PagerSlidingTabStrip emojiPagerIndicator = (PagerSlidingTabStrip) keyboardView.findViewById(R.id.emoji_pager_indicator);
        View backspace = keyboardView.findViewById(R.id.backspace);

        emojiPagerIndicator.setTabBackground(R.drawable.selector_bar);
        emojiPagerIndicator.setIndicatorColorResource(R.color.main_tab_selected);
        emojiPagerIndicator.setIndicatorHeight(Screen.dp(4));
        emojiPagerIndicator.setDividerColorResource(R.color.main_tab_divider);
        emojiPagerIndicator.setUnderlineHeight(0);

        backspace.setOnTouchListener(new RepeatListener(500,100, new OnClickListener() {
            @Override
            public void onClick(View v) {
                if(onEmojiconBackspaceClickedListener!=null)
                    onEmojiconBackspaceClickedListener.onClick(v);
            }
        }));

        mEmojisAdapter = new EmojisPagerAdapter(
                Arrays.asList(
                        EmojiPack.STANDART,
                        EmojiPack.NATURE,
                        EmojiPack.TRANSPORT,
                        EmojiPack.UNSORTED
                )
        );

        emojiPagerIndicator.setLayoutParams(new RelativeLayout.LayoutParams(Screen.dp(58 * mEmojisAdapter.getCount()), Screen.dp(48)));
        emojiPager.setAdapter(mEmojisAdapter);
        emojiPagerIndicator.setViewPager(emojiPager);


        return keyboardView;
    }

    @Override
    public void addRecentEmoji(Context context, long emojiCode) {
    }


    private class EmojisPagerAdapter extends PagerAdapter implements PagerSlidingTabStrip.IconTabProvider {

        private final List<long[]> emojiPacks;

        public EmojisPagerAdapter(List<long[]> emojiPacks) {
            super();
            this.emojiPacks = emojiPacks;
        }

        @Override
        public int getCount() {
            return emojiPacks.size();
        }


        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            View itemView = LayoutInflater.from(mContext).inflate(R.layout.emoji_page, null);
            ViewGroup emojicontainer = (ViewGroup) itemView.findViewById(R.id.emojimapcontainer);

            long[] emojiPack = emojiPacks.get(position);

            int emojisRowCount = Screen.getWidth() / Screen.dp(34);
            EmojiPackView emojiPackView = new EmojiPackView(mContext, core().getEmojiProcessor(), emojiPack, emojisRowCount, Screen.dp(34), Screen.dp(4));
            FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(emojisRowCount * Screen.dp(34), ViewGroup.LayoutParams.WRAP_CONTENT);
            params.gravity = Gravity.CENTER;
            emojicontainer.addView(emojiPackView, params);

            emojiPackView.setOnEmojiClickedListener(new EmojiPackView.OnEmojiClickedListener() {
                @Override
                public void onEmojiClicked(long smileId) {
                    if(onEmojiClickedListener!=null)
                        onEmojiClickedListener.onEmojiClicked(smileId);
                }
            });


            ((ViewPager) container).addView(itemView, 0);
            return itemView;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object view) {
            ((ViewPager) container).removeView((View) view);
        }

        @Override
        public boolean isViewFromObject(View view, Object key) {
            return key == view;
        }

        @Override
        public int getPageIconResId(int position) {
            return R.drawable.button_emoji;
        }
    }


    public static class RepeatListener implements View.OnTouchListener {

        private Handler handler = new Handler();

        private int initialInterval;
        private final int normalInterval;
        private final OnClickListener clickListener;

        private Runnable handlerRunnable = new Runnable() {
            @Override
            public void run() {
                if (downView == null) {
                    return;
                }
                handler.removeCallbacksAndMessages(downView);
                handler.postAtTime(this, downView, SystemClock.uptimeMillis() + normalInterval);
                clickListener.onClick(downView);
            }
        };

        private View downView;


        public RepeatListener(int initialInterval, int normalInterval, OnClickListener clickListener) {
            if (clickListener == null)
                throw new IllegalArgumentException("null runnable");
            if (initialInterval < 0 || normalInterval < 0)
                throw new IllegalArgumentException("negative interval");

            this.initialInterval = initialInterval;
            this.normalInterval = normalInterval;
            this.clickListener = clickListener;
        }

        public boolean onTouch(View view, MotionEvent motionEvent) {
            switch (motionEvent.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    downView = view;
                    handler.removeCallbacks(handlerRunnable);
                    handler.postAtTime(handlerRunnable, downView, SystemClock.uptimeMillis() + initialInterval);
                    clickListener.onClick(view);
                    return true;
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                case MotionEvent.ACTION_OUTSIDE:
                    handler.removeCallbacksAndMessages(downView);
                    downView = null;
                    return true;
            }
            return false;
        }
    }

    public interface OnSoftKeyboardOpenCloseListener {
        void onKeyboardOpen(int keyBoardHeight);

        void onKeyboardClose();
    }

    public interface OnEmojiconBackspaceClickedListener extends OnClickListener {

        @Override
        void onClick(View v);
    }
}
