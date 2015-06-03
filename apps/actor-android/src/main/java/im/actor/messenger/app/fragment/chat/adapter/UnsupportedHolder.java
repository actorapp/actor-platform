/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.messenger.app.fragment.chat.adapter;

import android.view.View;

import im.actor.messenger.R;
import im.actor.messenger.app.fragment.chat.MessagesAdapter;
import im.actor.model.entity.Message;

public class UnsupportedHolder extends TextHolder {

    private String text;

    public UnsupportedHolder(MessagesAdapter fragment, View itemView) {
        super(fragment, itemView, false);

        text = fragment.getMessagesFragment().getResources().getString(R.string.chat_unsupported);
    }

    @Override
    protected void bindData(Message message, boolean isUpdated) {
        bindRawText(text, message, true);
    }
}
