/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.model.modules;

import java.util.HashMap;

import im.actor.model.MessengerEnvironment;
import im.actor.model.droidkit.engine.ListEngine;
import im.actor.model.droidkit.engine.ListEngineDisplayExt;
import im.actor.model.entity.Contact;
import im.actor.model.entity.Dialog;
import im.actor.model.entity.Message;
import im.actor.model.entity.Peer;
import im.actor.model.entity.SearchEntity;
import im.actor.model.mvvm.BindedDisplayList;
import im.actor.model.mvvm.DisplayList;
import im.actor.model.mvvm.MVVMEngine;

public class DisplayLists extends BaseModule {
    private static final int LOAD_GAP = 5;
    private static final int LOAD_PAGE = 20;

    private final MessengerEnvironment environment;
    private final DisplayList.OperationMode operationMode;

    private BindedDisplayList<Dialog> dialogGlobalList;

    private BindedDisplayList<Contact> contactsGlobalList;

    private HashMap<Peer, BindedDisplayList<Message>> chatMediaGlobalLists = new HashMap<Peer, BindedDisplayList<Message>>();

    private HashMap<Peer, BindedDisplayList<Message>> chatsGlobalLists = new HashMap<Peer, BindedDisplayList<Message>>();

    public DisplayLists(MessengerEnvironment environment, Modules modules) {
        super(modules);
        this.environment = environment;
        switch (environment) {
            case ANDROID:
                operationMode = DisplayList.OperationMode.ANDROID;
                break;
            default:
                operationMode = DisplayList.OperationMode.GENERAL;
        }
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

    public BindedDisplayList<Message> getMessagesMediaList(Peer peer) {
        MVVMEngine.checkMainThread();

        if (!chatMediaGlobalLists.containsKey(peer)) {
            chatMediaGlobalLists.put(peer, buildMediaList(peer, true));
        }

        return chatMediaGlobalLists.get(peer);
    }

    public int getMediaCount(Peer peer) {
        ListEngine<Message> mediaEngine = modules().getMessagesModule().getMediaEngine(peer);
        return mediaEngine.getCount();
    }

    public BindedDisplayList<Dialog> buildNewDialogsList(boolean isGlobalList) {
        MVVMEngine.checkMainThread();

        ListEngine<Dialog> dialogsEngine = modules().getMessagesModule().getDialogsEngine();
        if (!(dialogsEngine instanceof ListEngineDisplayExt)) {
            throw new RuntimeException("Dialogs ListEngine must implement ListEngineDisplayExt for using global list");
        }

        BindedDisplayList.BindHook<Dialog> hook = null;
        if (isGlobalList) {
            hook = new BindedDisplayList.BindHook<Dialog>() {

                @Override
                public void onScrolledToEnd() {
                    modules().getMessagesModule().loadMoreDialogs();
                }

                @Override
                public void onItemTouched(Dialog item) {

                }
            };
        }
        BindedDisplayList<Dialog> displayList = new BindedDisplayList<Dialog>((ListEngineDisplayExt<Dialog>) dialogsEngine,
                isGlobalList, LOAD_PAGE, LOAD_GAP, hook);
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
                isGlobalList, LOAD_PAGE, LOAD_GAP, null);
        contactList.initTop(false);
        return contactList;
    }

    public BindedDisplayList<Message> buildNewChatList(final Peer peer, boolean isGlobalList) {
        MVVMEngine.checkMainThread();

        ListEngine<Message> messagesEngine = modules().getMessagesModule().getConversationEngine(peer);
        if (!(messagesEngine instanceof ListEngineDisplayExt)) {
            throw new RuntimeException("Conversation ListEngine must implement ListEngineDisplayExt for using global list");
        }

        BindedDisplayList.BindHook<Message> hook = null;
        if (isGlobalList) {
            hook = new BindedDisplayList.BindHook<Message>() {

                @Override
                public void onScrolledToEnd() {
                    modules().getMessagesModule().loadMoreHistory(peer);
                }

                @Override
                public void onItemTouched(Message item) {
                    if (item.getSenderId() != myUid()) {
                        modules().getMessagesModule().onInMessageShown(peer, item.getSortDate());
                    }
                }
            };
        }

        BindedDisplayList<Message> chatList = new BindedDisplayList<Message>((ListEngineDisplayExt<Message>) messagesEngine,
                isGlobalList, LOAD_PAGE, LOAD_GAP, hook);

        long lastRead = modules().getMessagesModule().loadReadState(peer);

        if(lastRead!=0)
            chatList.initCenter(lastRead, false);
        else
            chatList.initTop(false);
        return chatList;
    }

    public BindedDisplayList<Message> buildMediaList(final Peer peer, boolean isGlobalList) {
        MVVMEngine.checkMainThread();

        ListEngine<Message> mediaEngine = modules().getMessagesModule().getMediaEngine(peer);
        if (!(mediaEngine instanceof ListEngineDisplayExt)) {
            throw new RuntimeException("Media ListEngine must implement ListEngineDisplayExt for using global list");
        }

        BindedDisplayList<Message> mediaList = new BindedDisplayList<Message>((ListEngineDisplayExt<Message>) mediaEngine,
                isGlobalList, LOAD_PAGE, LOAD_GAP, null);
        mediaList.initTop(false);
        return mediaList;
    }

    public BindedDisplayList<SearchEntity> buildNewSearchList(boolean isGlobalList) {
        MVVMEngine.checkMainThread();

        ListEngine<SearchEntity> contactsEngine = modules().getSearch().getSearchList();
        if (!(contactsEngine instanceof ListEngineDisplayExt)) {
            throw new RuntimeException("Search ListEngine must implement ListEngineDisplayExt for using global list");
        }

        BindedDisplayList<SearchEntity> contactList = new BindedDisplayList<SearchEntity>((ListEngineDisplayExt<SearchEntity>) contactsEngine,
                isGlobalList, LOAD_PAGE, LOAD_GAP, null);
        contactList.initEmpty();
        return contactList;
    }
}
