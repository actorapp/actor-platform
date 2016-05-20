package im.actor.sdk.controllers.fragment.media;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;

import im.actor.core.entity.Message;
import im.actor.sdk.R;

import im.actor.sdk.controllers.conversation.messages.DocHolder;
import im.actor.sdk.controllers.conversation.messages.MessageHolder;
import im.actor.sdk.controllers.conversation.MessagesAdapter;
import im.actor.sdk.controllers.conversation.MessagesFragment;
import im.actor.sdk.controllers.conversation.messages.preprocessor.PreprocessedData;
import im.actor.sdk.controllers.conversation.view.BubbleContainer;
import im.actor.runtime.generic.mvvm.BindedDisplayList;

public class DocumentsAdapter extends MessagesAdapter {


    public DocumentsAdapter(BindedDisplayList<Message> displayList, MessagesFragment messagesFragment, Context context) {
        super(displayList, messagesFragment, context);
    }


    @Override
    public MessageHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        return new DocumentHolder(this, inflate(R.layout.adapter_document, viewGroup));
    }

    private class DocumentHolder extends DocHolder {

        public DocumentHolder(MessagesAdapter fragment, View itemView) {
            super(fragment, itemView, true);
        }

        @Override
        protected void bindData(Message message, long readDate, long receiveDate, boolean isUpdated, PreprocessedData preprocessedData) {
            super.bindData(message, readDate, receiveDate, isUpdated, preprocessedData);

            container.hideDate();
            container.setOnClickListener((View.OnClickListener) this);
            container.setOnClickListener((BubbleContainer.OnAvatarClickListener) this);
            container.setOnLongClickListener((View.OnLongClickListener) this);
            container.setOnLongClickListener((BubbleContainer.OnAvatarLongClickListener) this);
        }
    }
}