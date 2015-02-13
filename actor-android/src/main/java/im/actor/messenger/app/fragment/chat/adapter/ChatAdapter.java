package im.actor.messenger.app.fragment.chat.adapter;

import android.content.Context;

import com.droidkit.engine.list.view.ListState;

import im.actor.messenger.app.fragment.chat.MessagesFragment;
import im.actor.messenger.app.view.EngineHolderAdapter;
import im.actor.messenger.app.view.ViewHolder;
import im.actor.messenger.core.actors.chat.ConversationHistoryActor;
import im.actor.messenger.model.MessageModel;
import im.actor.messenger.model.DialogUids;
import im.actor.messenger.storage.ListEngines;
import im.actor.messenger.storage.scheme.messages.types.DocumentMessage;
import im.actor.messenger.storage.scheme.messages.types.PhotoMessage;
import im.actor.messenger.storage.scheme.messages.types.TextMessage;
import im.actor.messenger.storage.scheme.messages.types.VideoMessage;

public class ChatAdapter extends EngineHolderAdapter<MessageModel> {

    private static final int LOAD_GAP = 20;

    private int type, id;

    private MessagesFragment fragment;

    public ChatAdapter(int type, int id, MessagesFragment fragment, Context context) {
        super(ListEngines.messagesUiList(DialogUids.getDialogUid(type, id)), true, false, context);
        // ConversationHistoryActor.conv(type, id);
        this.fragment = fragment;
        this.type = type;
        this.id = id;
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
    public long getItemId(MessageModel obj) {
        return obj.getRaw().getRid();
    }

    @Override
    public int getItemViewType(int position) {
        MessageModel object = getItem(position);
        if (object.getContent() instanceof TextMessage) {
            return 0;
        } else if (object.getContent() instanceof VideoMessage ||
                object.getContent() instanceof PhotoMessage) {
            return 1;
        } else if (object.getContent() instanceof DocumentMessage) {
            return 2;
        } else {
            return 3;
        }
    }

    @Override
    public int getViewTypeCount() {
        return 4;
    }

    @Override
    protected ViewHolder<MessageModel> createHolder(MessageModel obj) {
        if (obj.getContent() instanceof TextMessage) {
            return new TextHolder(type, id, fragment, getUiList());
        } else if (obj.getContent() instanceof PhotoMessage ||
                obj.getContent() instanceof VideoMessage) {
            return new PhotoHolder(fragment, getUiList(), type, id);
        } else if (obj.getContent() instanceof DocumentMessage) {
            return new DocHolder(fragment, getUiList(), type, id);
        } else {
            return new ServiceHolder(type, id, fragment, getUiList());
        }
    }

    @Override
    protected void afterItemLoaded(MessageModel object, int position) {
        if (getEngine().getListState().getValue().getState() == ListState.State.LOADED) {
            if (position > getCount() - LOAD_GAP) {
                // ConversationHistoryActor.conv(type, id).onEndReached();
            }
        }
    }
}