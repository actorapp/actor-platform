package im.actor.sdk.controllers.fragment.media;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import im.actor.core.entity.Message;
import im.actor.messenger.R;

import im.actor.sdk.controllers.conversation.messages.DocHolder;
import im.actor.sdk.controllers.conversation.messages.MessageHolder;
import im.actor.sdk.controllers.conversation.messages.MessagesAdapter;
import im.actor.sdk.controllers.conversation.messages.MessagesFragment;
import im.actor.sdk.controllers.conversation.messages.PreprocessedData;
import im.actor.sdk.controllers.conversation.view.BubbleContainer;
import im.actor.runtime.generic.mvvm.BindedDisplayList;


/**
 * Created by ex3ndr on 26.02.15.
 */
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
        protected void bindData(Message message, boolean isUpdated, PreprocessedData preprocessedData) {
            super.bindData(message, isUpdated, preprocessedData);
            container.hideDate();
            container.setOnClickListener((View.OnClickListener) this);
            container.setOnClickListener((BubbleContainer.OnAvatarClickListener) this);
            container.setOnLongClickListener((View.OnLongClickListener) this);
            container.setOnLongClickListener((BubbleContainer.OnAvatarLongClickListener) this);
        }
    }

}