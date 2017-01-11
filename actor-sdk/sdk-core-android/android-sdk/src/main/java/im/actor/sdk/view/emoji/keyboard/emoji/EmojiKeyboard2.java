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
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.support.v4.view.ViewPager;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import im.actor.core.entity.Sticker;
import im.actor.sdk.ActorSDK;
import im.actor.sdk.R;
import im.actor.sdk.util.Screen;
import im.actor.sdk.view.MaterialInterpolator;
import im.actor.sdk.view.PagerSlidingTabStrip;
import im.actor.sdk.view.emoji.SmileProcessor;
import im.actor.sdk.view.emoji.keyboard.BaseKeyboard;
import im.actor.sdk.view.emoji.keyboard.BaseKeyboard2;
import im.actor.sdk.view.emoji.keyboard.emoji.smiles.OnBackspaceClickListener;
import im.actor.sdk.view.emoji.keyboard.emoji.smiles.OnSmileClickListener;
import im.actor.sdk.view.emoji.keyboard.emoji.smiles.RepeatListener;
import im.actor.sdk.view.emoji.keyboard.emoji.smiles.SmilePagerAdapter;
import im.actor.sdk.view.emoji.smiles.SmilesPack;

public class EmojiKeyboard2 extends BaseKeyboard2  {

    private static final String TAG = "EmojiKeyboard";
    private OnStickerClickListener onStickerClickListener;

    private static final long BINDING_DELAY = 150;

    public EmojiKeyboard2(Activity activity, EditText messageBody) {
        super(activity, messageBody);
    }


    @Override
    protected View createView() {


        EmojiView emojiView = new EmojiView(getActivity());
        emojiView.setVisibility(View.VISIBLE);

        emojiView.setListener(new EmojiView.Listener() {
            public boolean onBackspace() {
                if (messageBody == null) {
                    return true;
                }
                KeyEvent event = new KeyEvent(0, 0, 0, KeyEvent.KEYCODE_DEL, 0, 0, 0, 0, KeyEvent.KEYCODE_ENDCALL);
                messageBody.dispatchKeyEvent(event);
                return true;
            }

            public void onEmojiSelected(String symbol) {
                if (messageBody == null) {
                    return;
                }
                int selectionEnd = messageBody.getSelectionEnd();
                if (selectionEnd < 0) {
                    selectionEnd = messageBody.getText().length();
                }

                CharSequence localCharSequence = Emoji.replaceEmoji(symbol, messageBody.getPaint().getFontMetricsInt(), Screen.dp(20), false);

                messageBody.getText().insert(selectionEnd, localCharSequence);
            }


            @Override
            public void onClearEmojiRecent() {
                if (getActivity() == null) {
                    return;
                }
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setTitle(ActorSDK.sharedActor().getAppName());
                builder.setMessage("Remover emojis recentes");
                builder.setPositiveButton("Confirmar", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        emojiView.clearRecentEmoji();
                    }
                });
                builder.setNegativeButton("Cancelar", null);
                builder.create().show();
            }
        });
        return emojiView;
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

    public OnStickerClickListener getOnStickerClickListener() {
        return onStickerClickListener;
    }

    public void setOnStickerClickListener(OnStickerClickListener onStickerClickListener) {
        this.onStickerClickListener = onStickerClickListener;
    }
}