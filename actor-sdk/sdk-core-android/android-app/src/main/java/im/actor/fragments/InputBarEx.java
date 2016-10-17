package im.actor.fragments;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import im.actor.develop.R;
import im.actor.sdk.ActorSDK;
import im.actor.sdk.controllers.conversation.inputbar.InputBarCallback;
import im.actor.sdk.controllers.conversation.inputbar.InputBarFragment;
import im.actor.sdk.view.emoji.keyboard.KeyboardStatusListener;
import im.actor.sdk.view.emoji.keyboard.emoji.EmojiKeyboard;

import static im.actor.sdk.util.ViewUtils.zoomInView;
import static im.actor.sdk.util.ViewUtils.zoomOutView;

public class InputBarEx extends InputBarFragment {

    private boolean keyboardShown;
    private ImageView botKeyboardBtn;

    public InputBarEx() {
    }

    private boolean botKeyboardMode = false;
    private boolean botKeyboardAvailable = false;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View res = super.onCreateView(inflater, container, savedInstanceState);
        emojiButton.setOnClickListener(view -> {
            if (botKeyboardMode) {
                ((BotKeyboard) emojiKeyboard).show(false);
            } else {
                emojiKeyboard.toggle();
            }
        });
        botKeyboardBtn = (ImageView) res.findViewById(R.id.bot_keyboard);
        botKeyboardBtn.setOnClickListener(view -> {
            if (botKeyboardMode) {
                emojiKeyboard.toggle();
            } else {
                ((BotKeyboard) emojiKeyboard).show(true);
            }
        });
        emojiKeyboard.setKeyboardStatusListener(new BotKeyboardStatusListener() {

            @Override
            public void onBotKeyboardStatusChanged(boolean botKeyboardModeOn, boolean keyboardAvailable) {
                botKeyboardAvailable = keyboardAvailable;
                botKeyboardMode = botKeyboardModeOn;
                checkButtons();
            }

            @Override
            public void onDismiss() {
                keyboardShown = false;
                botKeyboardMode = false;
                checkButtons();
            }

            @Override
            public void onShow() {
                keyboardShown = true;
                checkButtons();
            }
        });
        return res;
    }

    protected void checkButtons() {
        emojiButton.setImageResource(botKeyboardMode || !keyboardShown ? R.drawable.ic_emoji : R.drawable.ic_keyboard);
        botKeyboardBtn.getLayoutParams().height = (botKeyboardAvailable ? ViewGroup.LayoutParams.MATCH_PARENT : 0);
        botKeyboardBtn.setImageResource(!botKeyboardMode || !keyboardShown ? R.drawable.ic_communication_textsms : R.drawable.ic_keyboard);
    }

    @Override
    public void onResume() {
        super.onResume();
        checkButtons();
    }

    @Override
    protected void checkSendButton(boolean hasText) {
        super.checkSendButton(hasText);
        if (botKeyboardAvailable) {
            if (hasText) {
                zoomOutView(botKeyboardBtn);
            } else {
                zoomInView(botKeyboardBtn);
            }
        }
    }

    @NonNull
    @Override
    protected EmojiKeyboard getEmojiKeyboard() {
        return new BotKeyboard(getActivity(), messageEditText, (content, isDraft, isSend) -> {
            if (isSend) {
                Fragment parent = getParentFragment();
                if (parent instanceof InputBarCallback) {
                    ((InputBarCallback) parent).onTextSent(content);
                }
            } else if (isDraft) {
                messageEditText.setText(content);
            }
        });
    }
}
