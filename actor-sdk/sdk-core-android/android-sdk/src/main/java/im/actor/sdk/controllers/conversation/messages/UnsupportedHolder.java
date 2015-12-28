/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.sdk.controllers.conversation.messages;

import android.view.View;

import im.actor.sdk.R;
import im.actor.core.entity.Message;

public class UnsupportedHolder extends TextHolder {

    protected String text;

    public UnsupportedHolder(MessagesAdapter fragment, View itemView) {
        super(fragment, itemView);

        text = fragment.getMessagesFragment().getResources().getString(R.string.chat_unsupported);
        onConfigureViewHolder();
    }

    @Override
    protected void bindData(Message message, boolean isUpdated, PreprocessedData preprocessedData) {
        bindRawText(text, null, message, true);
    }
}
