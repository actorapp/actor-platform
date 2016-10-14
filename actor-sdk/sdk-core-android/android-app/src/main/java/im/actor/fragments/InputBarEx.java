package im.actor.fragments;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import im.actor.sdk.controllers.conversation.inputbar.InputBarCallback;
import im.actor.sdk.controllers.conversation.inputbar.InputBarFragment;
import im.actor.sdk.view.emoji.keyboard.emoji.EmojiKeyboard;

public class InputBarEx extends InputBarFragment {

    public InputBarEx() {
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View res = super.onCreateView(inflater, container, savedInstanceState);

        return res;
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
