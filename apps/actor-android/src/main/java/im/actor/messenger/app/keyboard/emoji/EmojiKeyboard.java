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

package im.actor.messenger.app.keyboard.emoji;

import android.app.Activity;
import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.OrientationHelper;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.facebook.drawee.view.SimpleDraweeView;

import im.actor.messenger.R;
import im.actor.messenger.app.emoji.SmileProcessor;
import im.actor.messenger.app.emoji.stickers.Sticker;
import im.actor.messenger.app.emoji.stickers.StickersPack;
import im.actor.messenger.app.keyboard.BaseKeyboard;
import im.actor.messenger.app.keyboard.emoji.smiles.OnBackspaceClickListener;
import im.actor.messenger.app.keyboard.emoji.smiles.OnSmileClickListener;
import im.actor.messenger.app.keyboard.emoji.smiles.RepeatListener;
import im.actor.messenger.app.keyboard.emoji.smiles.SmilePagerAdapter;
import im.actor.messenger.app.keyboard.emoji.stickers.OnStickerClickListener;
import im.actor.messenger.app.keyboard.emoji.stickers.StickersFullpackAdapter;
import im.actor.messenger.app.util.Screen;
import im.actor.messenger.app.view.MaterialInterpolator;
import im.actor.messenger.app.view.PagerSlidingTabStrip;

import static im.actor.messenger.app.Core.getSmileProcessor;
import static im.actor.messenger.app.Core.getStickerProcessor;


public class EmojiKeyboard extends BaseKeyboard
        implements
        OnSmileClickListener,
        OnBackspaceClickListener,
        OnStickerClickListener {


    private static final String TAG = "EmojiKeyboard";
    private static final long BINDING_DELAY = 150;

    public EmojiKeyboard(Activity activity) {
        super(activity);
    }

    public void onEmojiClicked(String smile) {
        if (messageBody == null) {
            return;
        }
        int selectionEnd = messageBody.getSelectionEnd();
        if (selectionEnd < 0) {
            selectionEnd = messageBody.getText().length();
        }
        CharSequence appendString = getSmileProcessor().processEmojiMutable(smile,
                SmileProcessor.CONFIGURATION_BUBBLES);

        messageBody.getText().insert(selectionEnd, appendString);
    }

    OnStickerClickListener onStickerClickListener;

    @Override
    public void onBackspaceClick(View v) {
        if (messageBody == null) {
            return;
        }
        KeyEvent event = new KeyEvent(
                0, 0, 0, KeyEvent.KEYCODE_DEL, 0, 0, 0, 0, KeyEvent.KEYCODE_ENDCALL);
        messageBody.dispatchKeyEvent(event);
    }


    public void setOnStickerClickListener(OnStickerClickListener listener) {
        this.onStickerClickListener = listener;
    }

    @Override
    public void onStickerClick(Sticker sticker) {
        if (onStickerClickListener != null) {
            getStickerProcessor().upRecentSticker(sticker);
            onStickerClickListener.onStickerClick(sticker);
        }
    }

    @Override
    protected View createView() {

        View keyboardView = LayoutInflater.from(activity)
                .inflate(R.layout.emoji_keyboard, null);


        final ViewPager emojiPager = (ViewPager) keyboardView.findViewById(R.id.emojiContainer);
        emojiPager.setAdapter(new EmojiPagerAdapter());

        PagerSlidingTabStrip indicator = (PagerSlidingTabStrip) keyboardView.findViewById(R.id.indicator);
        indicator.setTabBackground(R.drawable.clickable_background);
        indicator.setIndicatorColorResource(R.color.primary);
        indicator.setIndicatorHeight(Screen.dp(2));
        indicator.setDividerColor(0x00000000);
        indicator.setUnderlineHeight(0);
        indicator.setTabPaddingLeftRight(0);
        indicator.setTabLayoutParams(new LinearLayout.LayoutParams(Screen.dp(50), Screen.dp(50)));
        indicator.setViewPager(emojiPager);


        return keyboardView;
    }

    @Override
    protected void onDismiss() {
        getSmileProcessor().getRecentController().saveRecents();
        getStickerProcessor().getRecentController().saveRecents();
    }

    class EmojiPagerAdapter extends PagerAdapter implements PagerSlidingTabStrip.TabProvider {
        @Override
        public int getCount() {
            return 2 + getStickerProcessor().getPacks().size();
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == object;
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            View item = null;
            if (position == 0) {
                item = createSmilesPager();
            } else {
                if (position == 1) {
                    item = createStickersRecentPagerView();
                } else {
                    item = createStickersPagerView(position - 2);
                }
            }

            container.addView(item, 0);
            return item;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView((View) object);
        }

        @Override
        public View getTab(final int position, Context context) {
            final long startTime = System.currentTimeMillis();
            final SimpleDraweeView tabView = new SimpleDraweeView(context);
            tabView.postDelayed(new Runnable() {
                @Override
                public void run() {
                    tabView.setAlpha(0f);
                    animateView(tabView);
                    tabView.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
                    tabView.setBackgroundResource(R.drawable.clickable_background);
                    if (position == 0) {
                        tabView.setImageResource(R.drawable.ic_smiles_smile);
                        tabView.setPadding(0, 0, 0, 0);
                    } else {
                        if (position == 1) {
                            tabView.setImageResource(R.drawable.ic_smiles_recent);
                            tabView.setPadding(0, 0, 0, 0);
                        } else {
                            StickersPack pack = getStickerProcessor().getPacks().get(position - 2);
                            final Sticker packLogo = pack.getLogoStickerId();
                            getStickerProcessor().bindSticker(tabView, packLogo);

                            tabView.setPadding(Screen.dp(5), Screen.dp(5), Screen.dp(5), Screen.dp(5));
                        }
                        Log.d(TAG, "Tab postCreated in " + (System.currentTimeMillis() - startTime));
                    }
                }
            }, BINDING_DELAY);

            Log.d(TAG, "Tab created in " + (System.currentTimeMillis() - startTime));
            return tabView;
        }
    }

    private View createStickersRecentPagerView() {
        return createStickersPagerView(-1);
    }


    private View createSmilesPager() {
        View emojiPagerView = LayoutInflater.from(activity).inflate(R.layout.emoji_smiles_pager, null);

        final ViewPager emojiPager = (ViewPager) emojiPagerView.findViewById(R.id.emoji_pager);


        final PagerSlidingTabStrip emojiPagerIndicator = (PagerSlidingTabStrip) emojiPagerView.findViewById(R.id.emoji_pager_indicator);
        View backspace = emojiPagerView.findViewById(R.id.backspace);

        emojiPagerIndicator.setTabBackground(R.drawable.clickable_background);
        emojiPagerIndicator.setIndicatorColorResource(R.color.primary);
        emojiPagerIndicator.setIndicatorHeight(Screen.dp(2));
        emojiPagerIndicator.setDividerColor(0x00000000);
        emojiPagerIndicator.setUnderlineHeight(0);
        emojiPagerIndicator.setTabLayoutParams(new LinearLayout.LayoutParams(Screen.dp(48), Screen.dp(48)));

        backspace.setOnTouchListener(new RepeatListener(500, 100, new OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackspaceClick(v);
            }
        }));

        final SmilePagerAdapter mEmojisAdapter = new SmilePagerAdapter(this);

        //emojiPagerIndicator.setLayoutParams(new RelativeLayout.LayoutParams(Screen.dp(58 * mEmojisAdapter.getCount()), Screen.dp(48)));
        emojiPager.postDelayed(new Runnable() {
            @Override
            public void run() {

                emojiPager.setAlpha(0f);
                emojiPagerIndicator.setAlpha(0f);
                animateView(emojiPager);
                animateView(emojiPagerIndicator);
                emojiPager.setAdapter(mEmojisAdapter);
                emojiPagerIndicator.setViewPager(emojiPager);
            }
        }, BINDING_DELAY);
        return emojiPagerView;
    }

    private View createStickersPagerView(final int packIndex) {
        /*View stickerPagerView = LayoutInflater.from(activity).inflate(R.layout.sticker_container_page, null);
        ViewPager stickerPager = (ViewPager) stickerPagerView.findViewById(R.id.sticker_pager);

        SitckersPagerAdapter stickersPager = new SitckersPagerAdapter(this, Stickers.getPacks()[packId]);

        stickerPager.setAdapter(stickersPager);
        return stickerPagerView;*/
        final RecyclerView recyclerView = new RecyclerView(activity);
        recyclerView.setLayoutManager(new StaggeredGridLayoutManager(4, OrientationHelper.VERTICAL));
        recyclerView.postDelayed(new Runnable() {
            @Override
            public void run() {
                recyclerView.setAlpha(0f);
                animateView(recyclerView);
                StickersPack pack;
                if (packIndex == -1) {
                    pack = getStickerProcessor().getRecentController().getPack();
                } else {
                    pack = getStickerProcessor().getPacks().get(packIndex);
                }
                recyclerView.setAdapter(new StickersFullpackAdapter(activity, EmojiKeyboard.this, pack, 0));
            }
        }, BINDING_DELAY);
        return recyclerView;
    }

    void animateView(View view) {
        view
                .animate()
                .setInterpolator(MaterialInterpolator.getInstance())
                .alpha(150)
                .setDuration(300)
                .start();
    }
}