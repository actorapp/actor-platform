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

package im.actor.sdk.view.emoji.keyboard.emoji;

import android.animation.ObjectAnimator;
import android.app.Activity;
import android.support.v4.view.ViewPager;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import im.actor.core.entity.Peer;
import im.actor.core.entity.Sticker;
import im.actor.sdk.R;
import im.actor.sdk.view.emoji.SmileProcessor;
import im.actor.sdk.view.emoji.keyboard.BaseKeyboard;
import im.actor.sdk.view.emoji.keyboard.emoji.smiles.OnBackspaceClickListener;
import im.actor.sdk.view.emoji.keyboard.emoji.smiles.OnSmileClickListener;
import im.actor.sdk.view.emoji.keyboard.emoji.smiles.RepeatListener;
import im.actor.sdk.view.emoji.keyboard.emoji.smiles.SmilePagerAdapter;
import im.actor.sdk.util.Screen;
import im.actor.sdk.view.MaterialInterpolator;
import im.actor.sdk.view.PagerSlidingTabStrip;
import im.actor.sdk.view.emoji.smiles.SmilesPack;

import static im.actor.sdk.util.ActorSDKMessenger.messenger;

public class EmojiKeyboard extends BaseKeyboard implements OnSmileClickListener, OnStickerClickListener,
        OnBackspaceClickListener {

    private static final String TAG = "EmojiKeyboard";

    private static final long BINDING_DELAY = 150;
    private Peer peer;
    private View stickerIndicatorContainer;
    private View stickerSwitchContainer;
    private SmilePagerAdapter mEmojisAdapter;


    public EmojiKeyboard(Activity activity) {
        super(activity);
    }

    @Override
    public void onEmojiClicked(String smile) {
        if (messageBody == null) {
            return;
        }
        int selectionEnd = messageBody.getSelectionEnd();
        if (selectionEnd < 0) {
            selectionEnd = messageBody.getText().length();
        }
        CharSequence appendString = SmileProcessor.emoji().processEmojiMutable(smile,
                SmileProcessor.CONFIGURATION_BUBBLES);

        messageBody.getText().insert(selectionEnd, appendString);
    }

    @Override
    public void onBackspaceClick(View v) {
        if (messageBody == null) {
            return;
        }
        KeyEvent event = new KeyEvent(0, 0, 0, KeyEvent.KEYCODE_DEL, 0, 0, 0, 0, KeyEvent.KEYCODE_ENDCALL);
        messageBody.dispatchKeyEvent(event);
    }

    @Override
    protected View createView() {
        final View emojiPagerView = LayoutInflater.from(activity).inflate(R.layout.emoji_smiles_pager, null);

        final ViewPager emojiPager = (ViewPager) emojiPagerView.findViewById(R.id.emoji_pager);

        final PagerSlidingTabStrip emojiPagerIndicator = (PagerSlidingTabStrip) emojiPagerView.findViewById(R.id.emoji_pager_indicator);
        View backspace = emojiPagerView.findViewById(R.id.backspace);
        final View backToSmiles = emojiPagerView.findViewById(R.id.back_to_smiles);
        final View indicatorContainer = emojiPagerView.findViewById(R.id.indicator_container);
        stickerIndicatorContainer = emojiPagerView.findViewById(R.id.sticker_indicator_container);
        stickerSwitchContainer = emojiPagerView.findViewById(R.id.sticker_switch_container);

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

        mEmojisAdapter = new SmilePagerAdapter(this);
        mEmojisAdapter.setTabs(emojiPagerIndicator);
        emojiPager.setAdapter(mEmojisAdapter);
        emojiPagerIndicator.setViewPager(emojiPager);

        backToSmiles.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                emojiPager.setCurrentItem(3, false);

                ObjectAnimator oa = ObjectAnimator.ofFloat(indicatorContainer, "translationX", 0, 0);
                oa.setDuration(0);
                oa.start();
                if (stickerIndicatorContainer.getVisibility() == View.INVISIBLE) {
                    stickerIndicatorContainer.setVisibility(View.VISIBLE);
                }
                ObjectAnimator oas = ObjectAnimator.ofFloat(stickerIndicatorContainer, "translationX", Screen.getWidth(), Screen.getWidth());
                oas.setDuration(0);
                oas.start();

                emojiPager.setCurrentItem(1, true);


            }
        });


        final FrameLayout.LayoutParams lp = (FrameLayout.LayoutParams) stickerIndicatorContainer.getLayoutParams();

        emojiPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                if (position == 4) {

                    ObjectAnimator oa = ObjectAnimator.ofFloat(indicatorContainer, "translationX", indicatorContainer.getX(), -positionOffsetPixels);
                    oa.setDuration(0);
                    oa.start();
                    if (stickerIndicatorContainer.getVisibility() == View.INVISIBLE) {
                        stickerIndicatorContainer.setVisibility(View.VISIBLE);
                    }
                    ObjectAnimator oas = ObjectAnimator.ofFloat(stickerIndicatorContainer, "translationX", stickerIndicatorContainer.getX() + Screen.getWidth(), -positionOffsetPixels + Screen.getWidth());
                    oas.setDuration(0);
                    oas.start();

                }
            }

            @Override
            public void onPageSelected(int position) {

            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        //emojiPagerIndicator.setLayoutParams(new RelativeLayout.LayoutParams(Screen.dp(58 * mEmojisAdapter.getCount()), Screen.dp(48)));
//        emojiPager.postDelayed(new Runnable() {
//            @Override
//            public void run() {
//                emojiPager.setAlpha(0f);
//                emojiPagerIndicator.setAlpha(0f);
//                animateView(emojiPager);
//                animateView(emojiPagerIndicator);
//                emojiPager.setAdapter(mEmojisAdapter);
//                emojiPagerIndicator.setViewPager(emojiPager);
//            }
//        }, BINDING_DELAY);

        if (SmilesPack.getRecent().size() == 0) {
            emojiPager.setCurrentItem(1);
        }

        return emojiPagerView;
    }

    @Override
    protected void onDismiss() {
        SmileProcessor.emoji().getRecentController().saveRecents();
    }

    void animateView(View view) {
        view.animate()
                .setInterpolator(MaterialInterpolator.getInstance())
                .alpha(150)
                .setDuration(300)
                .start();
    }

    public void setPeer(Peer peer) {
        this.peer = peer;
    }

    @Override
    public void onStickerClicked(Sticker sticker) {
        messenger().sendSticker(peer, sticker);
    }

    public LinearLayout getStickerIndicatorContainer() {
        return (LinearLayout) stickerSwitchContainer;
    }

    public void release() {
        if (mEmojisAdapter != null) {
            mEmojisAdapter.release();
        }
    }

}