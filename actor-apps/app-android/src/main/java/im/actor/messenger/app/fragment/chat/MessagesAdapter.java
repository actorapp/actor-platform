package im.actor.messenger.app.fragment.chat;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.HashMap;

import im.actor.android.view.BindedListAdapter;
import im.actor.messenger.R;
import im.actor.messenger.app.fragment.chat.adapter.DocHolder;
import im.actor.messenger.app.fragment.chat.adapter.MessageHolder;
import im.actor.messenger.app.fragment.chat.adapter.PhotoHolder;
import im.actor.messenger.app.fragment.chat.adapter.ServiceHolder;
import im.actor.messenger.app.fragment.chat.adapter.TextHolder;
import im.actor.messenger.app.fragment.chat.adapter.UnsupportedHolder;
import im.actor.model.entity.Message;
import im.actor.model.entity.content.AbsContent;
import im.actor.model.entity.content.DocumentContent;
import im.actor.model.entity.content.PhotoContent;
import im.actor.model.entity.content.ServiceContent;
import im.actor.model.entity.content.TextContent;
import im.actor.model.entity.content.VideoContent;
import im.actor.model.mvvm.BindedDisplayList;

/**
 * Created by ex3ndr on 26.02.15.
 */
public class MessagesAdapter extends BindedListAdapter<Message, MessageHolder> {

    private BaseMessagesFragment messagesFragment;
    private Context context;
    private long firstUnread = -1;
    private HashMap<Long, Message> selected = new HashMap<Long, Message>();
    private boolean isMarkDownEnabled;

    public MessagesAdapter(BindedDisplayList<Message> displayList, BaseMessagesFragment messagesFragment, Context context, boolean isMarkDownEnabled) {
        super(displayList);
        this.messagesFragment = messagesFragment;
        this.context = context;
        this.isMarkDownEnabled = isMarkDownEnabled;
    }

    public Message[] getSelected() {
        return selected.values().toArray(new Message[selected.size()]);
    }

    public boolean isSelected(Message msg) {
        return selected.containsKey(msg.getRid());
    }

    public void setSelected(Message msg, boolean isSelected) {
        if (isSelected) {
            selected.put(msg.getRid(), msg);
        } else {
            selected.remove(msg.getRid());
        }
        notifyDataSetChanged();
    }

    public int getSelectedCount() {
        return selected.size();
    }

    public void clearSelection() {
        selected.clear();
        notifyDataSetChanged();
    }

    public BaseMessagesFragment getMessagesFragment() {
        return messagesFragment;
    }

    public long getFirstUnread() {
        return firstUnread;
    }

    public void setFirstUnread(long firstUnread) {
        this.firstUnread = firstUnread;
    }

    @Override
    public int getItemViewType(int position) {
        AbsContent content = getItem(position).getContent();
        if (content instanceof TextContent) {
            return 0;
        } else if (content instanceof ServiceContent) {
            return 1;
        } else if (content instanceof PhotoContent) {
            return 2;
        } else if (content instanceof VideoContent) {
            return 2;
        } else if (content instanceof DocumentContent) {
            return 3;
        } else {
            return 4;
        }
    }

    private View inflate(int id, ViewGroup viewGroup) {
        return LayoutInflater
                .from(context)
                .inflate(id, viewGroup, false);
    }

    @Override
    public MessageHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        switch (viewType) {
            case 0:
                return new TextHolder(this, inflate(R.layout.adapter_dialog_text, viewGroup), isMarkDownEnabled);
            case 1:
                return new ServiceHolder(this, inflate(R.layout.adapter_dialog_service, viewGroup));
            case 2:
                return new PhotoHolder(this, inflate(R.layout.adapter_dialog_photo, viewGroup));
            case 3:
                return new DocHolder(this, inflate(R.layout.adapter_dialog_doc, viewGroup));
            default:
                return new UnsupportedHolder(this, inflate(R.layout.adapter_dialog_text, viewGroup));
        }
    }

    @Override
    public void onBindViewHolder(MessageHolder dialogHolder, int index, Message item) {
        Message prev = null;
        Message next = null;
        if (index > 1) {
            next = getItem(index - 1);
        }
        if (index < getItemCount() - 1) {
            prev = getItem(index + 1);
        }
        dialogHolder.bindData(item, prev, next);
    }

    @Override
    public void onViewRecycled(MessageHolder holder) {
        holder.unbind();
    }
}