/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.core.modules;

import java.util.HashMap;

import im.actor.core.entity.Contact;
import im.actor.core.entity.Dialog;
import im.actor.core.entity.Message;
import im.actor.core.entity.Peer;
import im.actor.core.entity.SearchEntity;
import im.actor.core.viewmodel.MessengerEnvironment;
import im.actor.runtime.mvvm.BindedDisplayList;
import im.actor.runtime.mvvm.DisplayList;
import im.actor.runtime.storage.ListEngine;
import im.actor.runtime.storage.ListEngineDisplayExt;

public class DisplayLists extends BaseModule {
    public static final int LOAD_GAP = 5;
    public static final int LOAD_PAGE = 20;

    private final MessengerEnvironment environment;
    private final DisplayList.OperationMode operationMode;

    private BindedDisplayList<Dialog> dialogGlobalList;

    private BindedDisplayList<Contact> contactsGlobalList;

    private HashMap<Peer, BindedDisplayList<Message>> chatMediaGlobalLists = new HashMap<Peer, BindedDisplayList<Message>>();

    private HashMap<Peer, BindedDisplayList<Message>> chatsGlobalLists = new HashMap<Peer, BindedDisplayList<Message>>();

    public DisplayLists(MessengerEnvironment environment, Modules modules) {
        super(modules);
        this.environment = environment;
        switch (environment){
            case ANDROID:
                this.operationMode = DisplayList.OperationMode.ANDROID;
                break;
            case IOS:
                this.operationMode = DisplayList.OperationMode.IOS;
                break;
            default:
                this.operationMode = DisplayList.OperationMode.GENERAL;
                break;
        }
    }

    public BindedDisplayList<Contact> getContactsGlobalList() {
        im.actor.runtime.Runtime.checkMainThread();

        if (contactsGlobalList == null) {
            contactsGlobalList = buildNewContactList(true);
        }

        return contactsGlobalList;
    }

    public BindedDisplayList<Dialog> getDialogsGlobalList() {
        im.actor.runtime.Runtime.checkMainThread();

        if (dialogGlobalList == null) {
            dialogGlobalList = buildNewDialogsList(true);
        }

        return dialogGlobalList;
    }

    public BindedDisplayList<Message> getMessagesGlobalList(Peer peer) {
        im.actor.runtime.Runtime.checkMainThread();

        if (!chatsGlobalLists.containsKey(peer)) {
            chatsGlobalLists.put(peer, buildNewChatList(peer, true));
        }

        return chatsGlobalLists.get(peer);
    }


    public BindedDisplayList<Dialog> buildNewDialogsList(boolean isGlobalList) {
        im.actor.runtime.Runtime.checkMainThread();

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
                isGlobalList, LOAD_PAGE, LOAD_GAP, operationMode, hook);
        displayList.initTop(false);
        return displayList;
    }

    public BindedDisplayList<Contact> buildNewContactList(boolean isGlobalList) {
        im.actor.runtime.Runtime.checkMainThread();

        ListEngine<Contact> contactsEngine = modules().getContactsModule().getContacts();
        if (!(contactsEngine instanceof ListEngineDisplayExt)) {
            throw new RuntimeException("Contacts ListEngine must implement ListEngineDisplayExt for using global list");
        }

        BindedDisplayList<Contact> contactList = new BindedDisplayList<Contact>((ListEngineDisplayExt<Contact>) contactsEngine,
                isGlobalList, LOAD_PAGE, LOAD_GAP, operationMode, null);
        contactList.initTop(false);
        return contactList;
    }

    public BindedDisplayList<Message> buildNewChatList(final Peer peer, boolean isGlobalList) {
        im.actor.runtime.Runtime.checkMainThread();

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
                    if (item.isOnServer()) {
                        modules().getMessagesModule().onMessageShown(peer, item.getSortDate());
                    }
                }
            };
        }

        // BaseAsyncStorageProvider storageProvider = (BaseAsyncStorageProvider) modules().getConfiguration().getStorageProvider();

        BindedDisplayList<Message> chatList = new BindedDisplayList<Message>((ListEngineDisplayExt<Message>) messagesEngine,
                isGlobalList, 20, 20, operationMode, hook);

        long lastRead = modules().getMessagesModule().loadReadState(peer);

        if (lastRead != 0)
            chatList.initCenter(lastRead, false);
        else
            chatList.initTop(false);
        return chatList;
    }


    public BindedDisplayList<SearchEntity> buildNewSearchList(boolean isGlobalList) {
        im.actor.runtime.Runtime.checkMainThread();

        ListEngine<SearchEntity> contactsEngine = modules().getSearch().getSearchList();
        if (!(contactsEngine instanceof ListEngineDisplayExt)) {
            throw new RuntimeException("Search ListEngine must implement ListEngineDisplayExt for using global list");
        }

        BindedDisplayList<SearchEntity> contactList = new BindedDisplayList<SearchEntity>((ListEngineDisplayExt<SearchEntity>) contactsEngine,
                isGlobalList, LOAD_PAGE, LOAD_GAP, operationMode, null);
        contactList.initEmpty();
        return contactList;
    }
}
