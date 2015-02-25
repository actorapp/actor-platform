package im.actor.messenger.storage;

import com.droidkit.engine.list.ListEngine;
import com.droidkit.engine.list.storage.SQLiteStorageAdapter;
import com.droidkit.engine.list.view.EngineUiList;
import com.droidkit.engine.list.view.ListHolder;

import java.util.HashMap;

import im.actor.messenger.storage.adapters.ContactsAdapter;
import im.actor.messenger.storage.adapters.DialogsAdapter;
import im.actor.messenger.storage.adapters.MessagesAdapter;
import im.actor.model.entity.Contact;
import im.actor.model.entity.Dialog;
import im.actor.model.entity.Message;
import im.actor.model.entity.Peer;

public final class ListEngines {

    private ListEngines() {
    }

    // Contacts
    private static final ListHolder<Contact> CONTACTS_ENGINE = new ListHolder<Contact>(new ContactsAdapter(), "CONTACTS");

    public static ListEngine<Contact> getContactsListEngine() {
        return CONTACTS_ENGINE.getEngine();
    }

    public static EngineUiList<Contact> getContactsUiListEngine() {
        return CONTACTS_ENGINE.getUiListEngine();
    }

    // Dialogs

    private static final ListHolder<Dialog> DIALOGS_ENGINE = new ListHolder<Dialog>(new DialogsAdapter(), "DIALOGS");

    public static ListEngine<Dialog> getChatsListEngine() {
        return DIALOGS_ENGINE.getEngine();
    }

    public static EngineUiList<Dialog> getChatsUiListEngine() {
        return DIALOGS_ENGINE.getUiListEngine();
    }

    // Conversations

    private static final HashMap<Peer, ConversationHolder> CONVERSATIONS = new HashMap<Peer, ConversationHolder>();

    private static ConversationHolder conversationHolder(Peer peer) {
        synchronized (CONVERSATIONS) {
            if (CONVERSATIONS.get(peer) == null) {
                CONVERSATIONS.put(peer, new ConversationHolder(peer));
            }
            return CONVERSATIONS.get(peer);
        }
    }

    public static ListEngine<Message> getMessages(Peer peer) {
        return conversationHolder(peer).getEngine();
    }

    public static EngineUiList<Message> getMessagesList(Peer peer) {
        return conversationHolder(peer).getUiList();
    }

    private static class ConversationHolder {
        private ListEngine<Message> engine;
        private EngineUiList<Message> uiList;

        private ConversationHolder(Peer peer) {

            SQLiteStorageAdapter storageAdapter = new SQLiteStorageAdapter(
                    SQLiteProvider.db(), "MESSAGES_" + peer.getUid());
            engine = new ListEngine<Message>(storageAdapter, new MessagesAdapter());
            uiList = new EngineUiList<Message>(engine, 20, false);

            // TODO: Implement
            uiList.initGeneral();

//            ReadState readState = readStates().get(chatUid);
//            if (readState == null || readState.getLastReadSortingKey() == 0) {
//                uiList.initGeneral();
//            } else {
//                uiList.scrollToItem(readState.getLastReadSortingKey());
//            }
        }

        public ListEngine<Message> getEngine() {
            return engine;
        }

        public EngineUiList<Message> getUiList() {
            return uiList;
        }
    }
}