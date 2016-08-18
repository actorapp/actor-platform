package im.actor.sdk.controllers.conversation.messages.content;

import android.view.View;

import im.actor.core.entity.Message;
import im.actor.runtime.android.view.BindedViewHolder;
import im.actor.sdk.controllers.conversation.messages.content.preprocessor.PreprocessedData;

public abstract class AbsMessageViewHolder extends BindedViewHolder {
    public AbsMessageViewHolder(View itemView) {
        super(itemView);
    }

    public abstract void bindData(Message message, Message prev, Message next, long readDate, long receiveDate, PreprocessedData preprocessedData);

    public abstract void unbind();
}
