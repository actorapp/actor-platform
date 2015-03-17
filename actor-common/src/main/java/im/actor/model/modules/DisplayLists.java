package im.actor.model.modules;

import java.util.HashMap;

import im.actor.model.droidkit.engine.ListEngine;
import im.actor.model.droidkit.engine.ListEngineDisplayExt;
import im.actor.model.entity.Contact;
import im.actor.model.entity.Dialog;
import im.actor.model.entity.Message;
import im.actor.model.entity.Peer;
import im.actor.model.mvvm.BindedDisplayList;
import im.actor.model.mvvm.MVVMEngine;

/**
 * Created by ex3ndr on 14.03.15.
 */
public class DisplayLists extends BaseModule {

    private static final int LOAD_GAP = 20;
    private static final int LOAD_PAGE = 20;

    private BindedDisplayList<Dialog> dialogGlobalList;

    private BindedDisplayList<Contact> contactsGlobalList;

    private HashMap<Peer, BindedDisplayList<Message>> chatsGlobalLists = new HashMap<Peer, BindedDisplayList<Message>>();

    public DisplayLists(Modules modules) {
        super(modules);
    }

    public BindedDisplayList<Contact> getContactsGlobalList() {
        MVVMEngine.checkMainThread();

        if (contactsGlobalList == null) {
            contactsGlobalList = buildNewContactList(true);
        }

        return contactsGlobalList;
    }

    public BindedDisplayList<Dialog> getDialogsGlobalList() {
        MVVMEngine.checkMainThread();

        if (dialogGlobalList == null) {
            dialogGlobalList = buildNewDialogsList(true);
        }

        return dialogGlobalList;
    }

    public BindedDisplayList<Message> getMessagesGlobalList(Peer peer) {
        MVVMEngine.checkMainThread();

        if (!chatsGlobalLists.containsKey(peer)) {
            chatsGlobalLists.put(peer, buildNewChatList(peer, true));
        }

        return chatsGlobalLists.get(peer);
    }

    public BindedDisplayList<Dialog> buildNewDialogsList(boolean isGlobalList) {
        MVVMEngine.checkMainThread();

        ListEngine<Dialog> dialogsEngine = modules().getMessagesModule().getDialogsEngine();
        if (!(dialogsEngine instanceof ListEngineDisplayExt)) {
            throw new RuntimeException("Dialogs ListEngine must implement ListEngineDisplayExt for using global list");
        }

        BindedDisplayList<Dialog> displayList = new BindedDisplayList<Dialog>((ListEngineDisplayExt<Dialog>) dialogsEngine,
                isGlobalList, LOAD_PAGE, LOAD_GAP);
        displayList.initTop(false);
        return displayList;
    }

    public BindedDisplayList<Contact> buildNewContactList(boolean isGlobalList) {
        MVVMEngine.checkMainThread();

        ListEngine<Contact> contactsEngine = modules().getContactsModule().getContacts();
        if (!(contactsEngine instanceof ListEngineDisplayExt)) {
            throw new RuntimeException("Contacts ListEngine must implement ListEngineDisplayExt for using global list");
        }

        BindedDisplayList<Contact> contactList = new BindedDisplayList<Contact>((ListEngineDisplayExt<Contact>) contactsEngine,
                isGlobalList, LOAD_PAGE, LOAD_GAP);
        contactList.initTop(false);
        return contactList;
    }

    public BindedDisplayList<Message> buildNewChatList(Peer peer, boolean isGlobalList) {
        MVVMEngine.checkMainThread();

        ListEngine<Message> messagesEngine = modules().getMessagesModule().getConversationEngine(peer);
        if (!(messagesEngine instanceof ListEngineDisplayExt)) {
            throw new RuntimeException("Conversation ListEngine must implement ListEngineDisplayExt for using global list");
        }

        BindedDisplayList<Message> chatList = new BindedDisplayList<Message>((ListEngineDisplayExt<Message>) messagesEngine,
                isGlobalList, LOAD_PAGE, LOAD_GAP);
        chatList.initTop(false);
        return chatList;
    }
}