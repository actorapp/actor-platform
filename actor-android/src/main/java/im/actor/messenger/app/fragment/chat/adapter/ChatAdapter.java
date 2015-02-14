package im.actor.messenger.app.fragment.chat.adapter;

import android.content.Context;

import com.droidkit.engine.list.view.ListState;

import im.actor.messenger.app.fragment.chat.MessagesFragment;
import im.actor.messenger.app.view.EngineHolderAdapter;
import im.actor.messenger.app.view.ViewHolder;
import im.actor.messenger.storage.ListEngines;
import im.actor.model.entity.Message;
import im.actor.model.entity.Peer;
import im.actor.model.entity.content.*;

public class ChatAdapter extends EngineHolderAdapter<Message> {

    private static final int LOAD_GAP = 20;

    private MessagesFragment fragment;
    private Peer peer;

    public ChatAdapter(Peer peer, MessagesFragment fragment, Context context) {
        super(ListEngines.getMessagesList(peer), true, false, context);
        this.fragment = fragment;
        this.peer = peer;
    }

    @Override
    public boolean areAllItemsEnabled() {
        return false;
    }

    @Override
    public boolean isEnabled(int position) {
        return false;
    }

    @Override
    public long getItemId(Message obj) {
        return obj.getRid();
    }

    @Override
    public int getItemViewType(int position) {
        Message object = getItem(position);
        if (object.getContent() instanceof TextContent) {
            return 0;
        } else if (object.getContent() instanceof VideoContent ||
                object.getContent() instanceof PhotoContent) {
            return 1;
        } else if (object.getContent() instanceof DocumentContent) {
            return 2;
        } else if (object.getContent() instanceof ServiceContent) {
            return 3;
        } else {
            throw new RuntimeException("Unknown content type");
        }
    }

    @Override
    public int getViewTypeCount() {
        return 4;
    }

    @Override
    protected ViewHolder<Message> createHolder(Message obj) {
        if (obj.getContent() instanceof TextContent) {
            return new TextHolder(peer, fragment, getUiList());
        } else if (obj.getContent() instanceof PhotoContent ||
                obj.getContent() instanceof VideoContent) {
            return new PhotoHolder(peer, fragment, getUiList());
        } else if (obj.getContent() instanceof DocumentContent) {
            return new DocHolder(peer, fragment, getUiList());
        } else if (obj.getContent() instanceof ServiceContent) {
            return new ServiceHolder(peer, fragment, getUiList());
        } else {
            throw new RuntimeException("Unknown content type");
        }
    }

    @Override
    protected void afterItemLoaded(Message object, int position) {
        if (getEngine().getListState().getValue().getState() == ListState.State.LOADED) {
            if (position > getCount() - LOAD_GAP) {
                // ConversationHistoryActor.conv(type, id).onEndReached();
            }
        }
    }
}