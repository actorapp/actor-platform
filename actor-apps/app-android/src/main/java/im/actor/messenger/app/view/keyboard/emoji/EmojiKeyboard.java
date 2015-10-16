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

package im.actor.messenger.app.view.keyboard.emoji;

import android.app.Activity;
import android.support.v4.view.ViewPager;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.LinearLayout;

import im.actor.messenger.R;
import im.actor.messenger.app.view.emoji.SmileProcessor;
import im.actor.messenger.app.view.keyboard.BaseKeyboard;
import im.actor.messenger.app.view.keyboard.emoji.smiles.OnBackspaceClickListener;
import im.actor.messenger.app.view.keyboard.emoji.smiles.OnSmileClickListener;
import im.actor.messenger.app.view.keyboard.emoji.smiles.RepeatListener;
import im.actor.messenger.app.view.keyboard.emoji.smiles.SmilePagerAdapter;
import im.actor.messenger.app.util.Screen;
import im.actor.messenger.app.view.MaterialInterpolator;
import im.actor.messenger.app.view.PagerSlidingTabStrip;

import static im.actor.messenger.app.core.Core.getSmileProcessor;

public class EmojiKeyboard extends BaseKeyboard implements OnSmileClickListener,
        OnBackspaceClickListener {

    private static final String TAG = "EmojiKeyboard";

    private static final long BINDING_DELAY = 150;

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
        CharSequence appendString = getSmileProcessor().processEmojiMutable(smile,
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

        emojiPager.setAdapter(mEmojisAdapter);
        emojiPagerIndicator.setViewPager(emojiPager);

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
        return emojiPagerView;
    }

    @Override
    protected void onDismiss() {
        getSmileProcessor().getRecentController().saveRecents();
    }

    void animateView(View view) {
        view.animate()
                .setInterpolator(MaterialInterpolator.getInstance())
                .alpha(150)
                .setDuration(300)
                .start();
    }
}