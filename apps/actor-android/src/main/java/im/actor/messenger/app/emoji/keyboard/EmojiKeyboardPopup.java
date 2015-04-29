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
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.SystemClock;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.OrientationHelper;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.view.WindowManager.LayoutParams;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;

import com.facebook.drawee.view.SimpleDraweeView;

import im.actor.messenger.R;
import im.actor.messenger.app.emoji.stickers.Stickers;
import im.actor.messenger.app.emoji.stickers.StickersAdapter;
import im.actor.messenger.app.emoji.stickers.StickersPack;
import im.actor.messenger.app.util.Screen;
import im.actor.messenger.app.view.PagerSlidingTabStrip;

import static im.actor.messenger.app.Core.core;


public class EmojiKeyboardPopup extends PopupWindow
        implements EmojiRecentsListener, OnStickerClickListener {
    private int keyBoardHeight = 0;
    private Boolean pendingOpen = false;
    private Boolean isOpened = false;
    OnEmojiClickListener onEmojiClickListener;
    OnStickerClickListener onStickerClickListener;
    OnEmojiconBackspaceClickedListener onEmojiconBackspaceClickedListener;
    OnSoftKeyboardOpenCloseListener onSoftKeyboardOpenCloseListener;
    Activity activity;
    private View decorView;
    private boolean softKeyboardListeningEnabled = true;


    public EmojiKeyboardPopup(Activity activity) {
        super(activity);
        this.activity = activity;
        decorView = activity.getWindow().getDecorView();
        setBackgroundDrawable(null);
        if (Build.VERSION.SDK_INT >= 21) {
            setElevation(0);
        }
        setContentView(createView());
        listenSoftKeyboard();
        //setSoftInputMode(LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
        //default size
        setSize(LayoutParams.MATCH_PARENT, (int) activity.getResources().getDimension(R.dimen.keyboard_height));
    }

    public void setOnSoftKeyboardOpenCloseListener(OnSoftKeyboardOpenCloseListener listener) {
        this.onSoftKeyboardOpenCloseListener = listener;
    }

    public void setOnEmojiClickListener(OnEmojiClickListener listener) {
        this.onEmojiClickListener = listener;
    }

    public void setOnEmojiconBackspaceClickedListener(OnEmojiconBackspaceClickedListener listener) {
        this.onEmojiconBackspaceClickedListener = listener;
    }

    public void setOnStickerClickListener(OnStickerClickListener listener) {
        this.onStickerClickListener = listener;
    }


    public void show() {
        if (isSoftwareKeyBoardOpened()) {
            showAtLocation(decorView, Gravity.BOTTOM, 0, 0);
        } else {
            resizeWindow(true);
            showAtLocation(decorView, Gravity.BOTTOM, 0, 0);
        }
    }

    private void resizeWindow(boolean keyboard) {

        if(keyboard){
            softKeyboardListeningEnabled = false;
            decorView.invalidate();
            decorView.requestLayout();
        } else {
            if (!softKeyboardListeningEnabled) {
                softKeyboardListeningEnabled = true;
            }
        }
    }


    public void showAtBottomPending() {
        if (isSoftwareKeyBoardOpened())
            show();
        else
            pendingOpen = true;
    }


    public Boolean isSoftwareKeyBoardOpened() {
        return isOpened;
    }


    @Override
    public void dismiss() {
        super.dismiss();
        if(!isSoftwareKeyBoardOpened()){
            resizeWindow(false);
        }
    }


    public void listenSoftKeyboard() {
        decorView.getViewTreeObserver().addOnGlobalLayoutListener(new OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                if(!softKeyboardListeningEnabled){
                    return;
                }
                Rect r = new Rect();
                decorView.getWindowVisibleDisplayFrame(r);

                int screenHeight = decorView.getRootView()
                        .getHeight();
                int heightDifference = screenHeight
                        - (r.bottom - r.top);
                int resourceId = activity.getResources()
                        .getIdentifier("status_bar_height",
                                "dimen", "android");
                if (resourceId > 0) {
                    heightDifference -= activity.getResources()
                            .getDimensionPixelSize(resourceId);
                }
                int orientation = activity.getResources().getConfiguration().orientation;

                int id = activity.getResources().getIdentifier("config_showNavigationBar", "bool", "android");
                if (id > 0) {
                    if (activity.getResources().getBoolean(id)) {
                        int navbarResId = activity.getResources()
                                .getIdentifier(
                                        orientation == Configuration.ORIENTATION_PORTRAIT ? "navigation_bar_height" : "navigation_bar_height_landscape",
                                        "dimen", "android");
                        if (navbarResId > 0) {
                            heightDifference -= activity.getResources()
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
                } else {
                    isOpened = false;
                    if (onSoftKeyboardOpenCloseListener != null)
                        onSoftKeyboardOpenCloseListener.onKeyboardClose();
                    if(isShowing())
                        dismiss();
                }
            }
        });
    }

    public void setSize(int width, int height) {
        setWidth(width);
        setHeight(height);
    }

    private View createView() {
        View keyboardView = LayoutInflater.from(activity)
                .inflate(R.layout.emoji_keyboard, null);


        final FrameLayout emojiContainer = (FrameLayout) keyboardView.findViewById(R.id.emojiContainer);
        emojiContainer.addView(createSmilesPager());

        RecyclerView recyclerView = (RecyclerView) keyboardView.findViewById(R.id.indicator);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(activity, OrientationHelper.HORIZONTAL, false));
        recyclerView.setAdapter(new RecyclerView.Adapter() {
            public int selected = 0;

            @Override
            public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                SimpleDraweeView imageView = new SimpleDraweeView(activity);
                imageView.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
                imageView.setLayoutParams(new RecyclerView.LayoutParams(Screen.dp(50), Screen.dp(50)));
                imageView.setBackgroundResource(R.drawable.clickable_background);
                return new RecyclerView.ViewHolder(imageView) {
                };
            }

            @Override
            public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position) {
                holder.itemView.setSelected(position == selected);
                holder.itemView.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        v.setSelected(true);
                        int oldSelected = selected;
                        selected = position;
                        notifyItemChanged(oldSelected);
                        View view = null;
                        if (position == 0) {
                            view = createSmilesPager();
                        } else {
                            view = createStickersPagerView(position - 1);
                        }
                        emojiContainer.removeAllViews();
                        emojiContainer.addView(view);
                    }
                });
                SimpleDraweeView packImageView = (SimpleDraweeView) holder.itemView;
                if (position == 0) {
                    packImageView.setImageResource(R.drawable.ic_emoji);
                    packImageView.setPadding(0, 0, 0, 0);
                } else {
                    StickersPack pack = Stickers.getPacks()[position - 1];
                    packImageView.setImageURI(Uri.parse("file://" + Stickers.getFile(pack.getId(), pack.getLogoStickerId())));
                    packImageView.setPadding(Screen.dp(5), Screen.dp(5), Screen.dp(5), Screen.dp(5));
                }
            }

            @Override
            public int getItemCount() {
                return 3;
            }
        });
        //emojiContainer.addView(createSmilesPager());

        /**/


        return keyboardView;
    }

    @Override
    public void addRecentEmoji(Context context, long emojiCode) {
    }

    @Override
    public void onStickerClick(String packId, String stickerId) {
        if (onStickerClickListener != null) {
            onStickerClickListener.onStickerClick(packId, stickerId);
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

    public View createSmilesPager() {
        View emojiPagerView = LayoutInflater.from(activity).inflate(R.layout.emoji_smiles_pager, null);

        ViewPager emojiPager = (ViewPager) emojiPagerView.findViewById(R.id.emoji_pager);


        PagerSlidingTabStrip emojiPagerIndicator = (PagerSlidingTabStrip) emojiPagerView.findViewById(R.id.emoji_pager_indicator);
        View backspace = emojiPagerView.findViewById(R.id.backspace);

        emojiPagerIndicator.setTabBackground(R.drawable.clickable_background);
        emojiPagerIndicator.setIndicatorColorResource(R.color.primary);
        emojiPagerIndicator.setIndicatorHeight(Screen.dp(2));
        emojiPagerIndicator.setDividerColor(0x00000000);
        emojiPagerIndicator.setUnderlineHeight(0);
        emojiPagerIndicator.setTabPaddingLeftRight(0);

        backspace.setOnTouchListener(new RepeatListener(500, 100, new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (onEmojiconBackspaceClickedListener != null)
                    onEmojiconBackspaceClickedListener.onClick(v);
            }
        }));

        EmojisPagerAdapter mEmojisAdapter = new EmojisPagerAdapter();

        emojiPagerIndicator.setLayoutParams(new RelativeLayout.LayoutParams(Screen.dp(58 * mEmojisAdapter.getCount()), Screen.dp(48)));
        emojiPager.setAdapter(mEmojisAdapter);
        emojiPagerIndicator.setViewPager(emojiPager);
        return emojiPagerView;
    }

    public View createStickersPagerView(int packId) {
        View stickerPagerView = LayoutInflater.from(activity).inflate(R.layout.sticker_container_page, null);
        ViewPager stickerPager = (ViewPager) stickerPagerView.findViewById(R.id.sticker_pager);

        SitckersPagerAdapter stickersPager = new SitckersPagerAdapter(Stickers.getPacks()[packId]);

        stickerPager.setAdapter(stickersPager);
        return stickerPagerView;
    }


    private class EmojisPagerAdapter extends PagerAdapter implements PagerSlidingTabStrip.TabProvider {

        @Override
        public int getCount() {
            return 4;
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            View itemView = LayoutInflater.from(container.getContext()).inflate(R.layout.emoji_smiles_page, null);
            ViewGroup emojicontainer = (ViewGroup) itemView.findViewById(R.id.emojiPackContainer);

            long[] emojiPack = new long[0];
            switch (position) {
                case 0:
                    emojiPack = EmojiPack.STANDART;
                    break;
                case 1:
                    emojiPack = EmojiPack.NATURE;
                    break;
                case 2:
                    emojiPack = EmojiPack.TRANSPORT;
                    break;
                case 3:
                    emojiPack = EmojiPack.UNSORTED;
                    break;
            }

            int emojisMaxRowCount = 8;
            int emojiSize = Screen.dp(45);
            int emojiPadding = emojiSize/5;
            if(Screen.getWidth()/emojiSize<emojisMaxRowCount){
                emojisMaxRowCount = Screen.getWidth()/emojiSize;
            }
            EmojiPackView emojiPackView = new EmojiPackView(activity, core().getEmojiProcessor(), emojiPack, emojisMaxRowCount, emojiSize, emojiPadding);
            FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            params.gravity = Gravity.CENTER;
            emojicontainer.addView(emojiPackView, params);

            emojiPackView.setOnEmojiClickListener(new OnEmojiClickListener() {
                @Override
                public void onEmojiClicked(String smile) {
                    if (onEmojiClickListener != null)
                        onEmojiClickListener.onEmojiClicked(smile);
                }
            });

            container.addView(itemView, 0);
            return itemView;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object view) {
            container.removeView((View) view);
        }

        @Override
        public boolean isViewFromObject(View view, Object key) {
            return key == view;
        }

        @Override
        public View getTab(int position) {

            ImageButton tabView = new ImageButton(activity);
            //if(position==0){
            tabView.setImageResource(R.drawable.ic_emoji);
            tabView.setPadding(24, 0, 24, 0);
            //} else{
                    /*tabView.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
                    tabView.setAdjustViewBounds(true);
                    //tabView.setCropToPadding(false);
                    StickersPack pack = Stickers.getPacks()[position - 1];
                    tabView.setImageURI(Uri.parse(Stickers.getFile(pack.getId(), pack.getLogoStickerId())));*/
            //}
            return tabView;
        }

    }

    private class SitckersPagerAdapter extends PagerAdapter {

        private final StickersPack stickersPack;

        public SitckersPagerAdapter(StickersPack stickersPack) {
            this.stickersPack = stickersPack;
        }

        @Override
        public int getCount() {
            int pages = 0;
            pages = stickersPack.size() / 8;
            if (stickersPack.size() % 8 > 0) {
                pages++;
            }
            return pages;
        }


        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            View page = LayoutInflater.from(activity).inflate(R.layout.sticker_item_page, null);
            RecyclerView recycler = (RecyclerView) page.findViewById(R.id.recycler);
            recycler.setLayoutManager(new GridLayoutManager(activity, 4));
            recycler.setAdapter(new StickersAdapter(activity, EmojiKeyboardPopup.this, stickersPack, position, (keyBoardHeight - Screen.dp(50)) / 2));
            container.addView(page);
            return page;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object view) {
            container.removeView((View) view);
        }

        @Override
        public boolean isViewFromObject(View view, Object key) {
            return key == view;
        }

    }

}