package im.actor.messenger.storage;

import com.droidkit.engine.list.*;
import com.droidkit.engine.list.storage.SQLiteStorageAdapter;
import com.droidkit.engine.list.view.*;

import im.actor.messenger.core.AppContext;
import im.actor.messenger.model.MessageModel;
import im.actor.messenger.storage.adapters.ListBoxedBserAdapter;
import im.actor.messenger.storage.adapters.ListBserAdapter;
import im.actor.messenger.storage.adapters.ListBserFilterableAdapter;
import im.actor.messenger.storage.scheme.Contact;
import im.actor.messenger.storage.scheme.media.Document;
import im.actor.messenger.storage.scheme.messages.ReadState;
import im.actor.messenger.storage.scheme.messages.DialogItem;

import java.util.HashMap;

import static im.actor.messenger.storage.KeyValueEngines.readStates;

public final class ListEngines {

    private ListEngines() {
    }

    private static final HashMap<Long, ConversationHolder> CHAT_ENGINES = new HashMap<Long, ConversationHolder>();

    private static ConversationHolder conversationHolder(long chatUid) {
        synchronized (CHAT_ENGINES) {
            if (CHAT_ENGINES.get(chatUid) == null) {
                CHAT_ENGINES.put(chatUid, new ConversationHolder(chatUid));
            }
            return CHAT_ENGINES.get(chatUid);
        }
    }

    public static ListEngine<Document> getDocuments(long chatUid) {
        return conversationHolder(chatUid).getDocuments().getEngine();
    }

    public static EngineUiList<Document> getDocumentsList(long chatUid) {
        return conversationHolder(chatUid).getDocuments().getUiListEngine();
    }

    public static ListEngine<MessageModel> getMessagesListEngine(long chatUid) {
        return conversationHolder(chatUid).getEngine();
    }

    public static EngineUiList<MessageModel> messagesUiList(long chatUid) {
        return conversationHolder(chatUid).getUiList();
    }

    private static final ListHolder<DialogItem> CHAT_LIST_ENGINE = new ListHolder<DialogItem>(new ListBserAdapter<DialogItem>(DialogItem.class), "CHATS");

    private static final ListHolder<Contact> CONTACTS_ENGINE = new ListHolder<Contact>(new ListBserFilterableAdapter<Contact>(Contact.class), "CONTACTS");

    public static ListEngine<DialogItem> getChatsListEngine() {
        return CHAT_LIST_ENGINE.getEngine();
    }

    public static EngineUiList<DialogItem> getChatsUiListEngine() {
        return CHAT_LIST_ENGINE.getUiListEngine();
    }

    public static ListEngine<Contact> getContactsEngine() {
        return CONTACTS_ENGINE.getEngine();
    }

    public static EngineUiList<Contact> getContactsList() {
        return CONTACTS_ENGINE.getUiListEngine();
    }

    private static class MessagesAdapter extends ListBoxedBserAdapter<MessageModel, im.actor.messenger.storage.scheme.messages.ConversationMessage> {

        public MessagesAdapter() {
            super(im.actor.messenger.storage.scheme.messages.ConversationMessage.class);
        }

        @Override
        public long getSortKey(MessageModel value) {
            return value.getSortingKey();
        }

        @Override
        public long getId(MessageModel value) {
            return value.getRid();
        }

        @Override
        protected long getRawId(im.actor.messenger.storage.scheme.messages.ConversationMessage raw) {
            return raw.getRid();
        }

        @Override
        protected im.actor.messenger.storage.scheme.messages.ConversationMessage convertToRaw(MessageModel raw) {
            return raw.getRaw();
        }

        @Override
        protected MessageModel convertToObj(im.actor.messenger.storage.scheme.messages.ConversationMessage raw) {
            return new MessageModel(raw);
        }

        @Override
        protected void updateObject(MessageModel obj, im.actor.messenger.storage.scheme.messages.ConversationMessage value) {
            obj.update(value);
        }
    }

    private static class ConversationHolder {
        private long chatUid;
        private ListEngine<MessageModel> engine;
        private EngineUiList<MessageModel> uiList;
        private ListHolder<Document> documents;

        private ConversationHolder(long chatUid) {
            this.chatUid = chatUid;

            MessagesAdapter dataAdapter = new MessagesAdapter();
            SQLiteStorageAdapter storageAdapter = new SQLiteStorageAdapter(
                    DbProvider.getDatabase(AppContext.getContext()),
                    "MESSAGES" + chatUid);
            engine = new ListEngine<MessageModel>(storageAdapter, dataAdapter);
            uiList = new EngineUiList<MessageModel>(engine, 20, false);

            ReadState readState = readStates().get(chatUid);
            if (readState == null || readState.getLastReadSortingKey() == 0) {
                uiList.initGeneral();
            } else {
                uiList.scrollToItem(readState.getLastReadSortingKey());
            }

            documents = new ListHolder<Document>(new ListBserFilterableAdapter<Document>(Document.class), "DOCUMENTS_" + chatUid);
        }

        public long getChatUid() {
            return chatUid;
        }

        public ListEngine<MessageModel> getEngine() {
            return engine;
        }

        public EngineUiList<MessageModel> getUiList() {
            return uiList;
        }

        public ListHolder<Document> getDocuments() {
            return documents;
        }
    }
}