package im.actor.fragments;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import im.actor.sdk.controllers.conversation.inputbar.InputBarFragment;
import im.actor.sdk.view.emoji.keyboard.emoji.EmojiKeyboard;

public class InputBarEx extends InputBarFragment {
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View res = super.onCreateView(inflater, container, savedInstanceState);

        return res;
    }

    @NonNull
    @Override
    protected EmojiKeyboard getEmojiKeyboard() {
        return new BotKeyboard(getActivity(), messageEditText);
    }
}
