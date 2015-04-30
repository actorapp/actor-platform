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
import android.net.Uri;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.OrientationHelper;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.facebook.drawee.view.SimpleDraweeView;

import im.actor.messenger.R;
import im.actor.messenger.app.keyboard.BaseKeyboard;
import im.actor.messenger.app.keyboard.emoji.smiles.OnBackspaceClickListener;
import im.actor.messenger.app.keyboard.emoji.smiles.OnSmileClickListener;
import im.actor.messenger.app.keyboard.emoji.smiles.RepeatListener;
import im.actor.messenger.app.keyboard.emoji.smiles.SmilePagerAdapter;
import im.actor.messenger.app.keyboard.emoji.stickers.OnStickerClickListener;
import im.actor.messenger.app.keyboard.emoji.stickers.SitckersPagerAdapter;
import im.actor.messenger.app.keyboard.emoji.stickers.Stickers;
import im.actor.messenger.app.keyboard.emoji.stickers.StickersFullpackAdapter;
import im.actor.messenger.app.keyboard.emoji.stickers.StickersPack;
import im.actor.messenger.app.util.Screen;
import im.actor.messenger.app.view.PagerSlidingTabStrip;

import static im.actor.messenger.app.Core.core;


public class EmojiKeyboard extends BaseKeyboard
        implements
        OnSmileClickListener,
        OnBackspaceClickListener,
        OnStickerClickListener {


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
        CharSequence appendString = core().getSmileProcessor().processEmojiMutable(smile,
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
    public void onStickerClick(String packId, String stickerId) {
        if (onStickerClickListener != null) {
            onStickerClickListener.onStickerClick(packId, stickerId);
        }
    }

    @Override
    protected View createView() {
        View keyboardView = LayoutInflater.from(activity)
                .inflate(R.layout.emoji_keyboard, null);


        final ViewPager emojiPager = (ViewPager) keyboardView.findViewById(R.id.emojiContainer);
        emojiPager.setAdapter(new EmojiPagerAdapter());
        emojiPager.addView(createSmilesPager());

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

    class EmojiPagerAdapter extends PagerAdapter implements PagerSlidingTabStrip.TabProvider {
        @Override
        public int getCount() {
            return 1 + Stickers.getPacks().length;
            // todo count stickers packs?
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
                item = createStickersPagerView(position - 1);
            }

            container.addView(item, 0);
            return item;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView((View) object);
        }

        @Override
        public View getTab(int position, Context context) {

            SimpleDraweeView tabView = new SimpleDraweeView(context);
            tabView.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
            tabView.setBackgroundResource(R.drawable.clickable_background);
            if (position == 0) {
                tabView.setImageResource(R.drawable.ic_emoji);
                tabView.setPadding(0, 0, 0, 0);
            } else {
                StickersPack pack = Stickers.getPacks()[position - 1];
                tabView.setImageURI(Uri.parse("file://" + Stickers.getFile(pack.getId(), pack.getLogoStickerId())));
                tabView.setPadding(Screen.dp(5), Screen.dp(5), Screen.dp(5), Screen.dp(5));
            }
            return tabView;
        }
    }


    private View createSmilesPager() {
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
                onBackspaceClick(v);
            }
        }));

        SmilePagerAdapter mEmojisAdapter = new SmilePagerAdapter(this);

        emojiPagerIndicator.setLayoutParams(new RelativeLayout.LayoutParams(Screen.dp(58 * mEmojisAdapter.getCount()), Screen.dp(48)));
        emojiPager.setAdapter(mEmojisAdapter);
        emojiPagerIndicator.setViewPager(emojiPager);
        return emojiPagerView;
    }

    private View createStickersPagerView(int packId) {
        /*View stickerPagerView = LayoutInflater.from(activity).inflate(R.layout.sticker_container_page, null);
        ViewPager stickerPager = (ViewPager) stickerPagerView.findViewById(R.id.sticker_pager);

        SitckersPagerAdapter stickersPager = new SitckersPagerAdapter(this, Stickers.getPacks()[packId]);

        stickerPager.setAdapter(stickersPager);
        return stickerPagerView;*/
        RecyclerView recyclerView = new RecyclerView(activity);
        recyclerView.setLayoutManager(new StaggeredGridLayoutManager(4, OrientationHelper.VERTICAL));
        recyclerView.setAdapter(new StickersFullpackAdapter(activity, this, Stickers.getPacks()[packId], 0));
        return recyclerView;
    }

}