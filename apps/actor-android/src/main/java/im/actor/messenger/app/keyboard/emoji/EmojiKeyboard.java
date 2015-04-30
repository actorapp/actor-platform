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
import android.graphics.PixelFormat;
import android.net.Uri;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.OrientationHelper;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.facebook.drawee.view.SimpleDraweeView;

import im.actor.messenger.R;
import im.actor.messenger.app.keyboard.BaseKeyboard;
import im.actor.messenger.app.keyboard.KeyboardStatusListener;
import im.actor.messenger.app.keyboard.emoji.smiles.OnBackspaceClickListener;
import im.actor.messenger.app.keyboard.emoji.smiles.OnSmileClickListener;
import im.actor.messenger.app.keyboard.emoji.smiles.RepeatListener;
import im.actor.messenger.app.keyboard.emoji.stickers.OnStickerClickListener;
import im.actor.messenger.app.keyboard.emoji.stickers.SitckersPagerAdapter;
import im.actor.messenger.app.keyboard.emoji.stickers.Stickers;
import im.actor.messenger.app.keyboard.emoji.stickers.StickersPack;
import im.actor.messenger.app.util.Screen;
import im.actor.messenger.app.view.PagerSlidingTabStrip;
import im.actor.model.log.Log;

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


        return keyboardView;
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

        EmojiPagerAdapter mEmojisAdapter = new EmojiPagerAdapter(this);

        emojiPagerIndicator.setLayoutParams(new RelativeLayout.LayoutParams(Screen.dp(58 * mEmojisAdapter.getCount()), Screen.dp(48)));
        emojiPager.setAdapter(mEmojisAdapter);
        emojiPagerIndicator.setViewPager(emojiPager);
        return emojiPagerView;
    }

    private View createStickersPagerView(int packId) {
        View stickerPagerView = LayoutInflater.from(activity).inflate(R.layout.sticker_container_page, null);
        ViewPager stickerPager = (ViewPager) stickerPagerView.findViewById(R.id.sticker_pager);

        SitckersPagerAdapter stickersPager = new SitckersPagerAdapter(this, Stickers.getPacks()[packId]);

        stickerPager.setAdapter(stickersPager);
        return stickerPagerView;
    }

}