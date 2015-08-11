/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.core.modules.internal;

import java.util.HashMap;

import im.actor.core.entity.Contact;
import im.actor.core.entity.Dialog;
import im.actor.core.entity.Message;
import im.actor.core.entity.Peer;
import im.actor.core.entity.SearchEntity;
import im.actor.core.modules.AbsModule;
import im.actor.core.modules.ModuleContext;
import im.actor.runtime.Storage;
import im.actor.runtime.mvvm.DisplayList;

public class DisplayLists extends AbsModule {

    private DisplayList<Dialog> dialogGlobalList;

    private DisplayList<Contact> contactsGlobalList;

    private HashMap<Peer, DisplayList<Message>> chatsGlobalLists = new HashMap<Peer, DisplayList<Message>>();

    public DisplayLists(ModuleContext context) {
        super(context);
    }

    public DisplayList<Contact> getContactsSharedList() {
        im.actor.runtime.Runtime.checkMainThread();

        if (contactsGlobalList == null) {
            contactsGlobalList = buildContactList(true);
        }

        return contactsGlobalList;
    }

    public DisplayList<Dialog> getDialogsSharedList() {
        im.actor.runtime.Runtime.checkMainThread();

        if (dialogGlobalList == null) {
            dialogGlobalList = buildDialogsList(true);
        }

        return dialogGlobalList;
    }

    public DisplayList<Message> getMessagesSharedList(Peer peer) {
        im.actor.runtime.Runtime.checkMainThread();

        if (!chatsGlobalLists.containsKey(peer)) {
            chatsGlobalLists.put(peer, buildChatList(peer, true));
        }

        return chatsGlobalLists.get(peer);
    }


    public DisplayList<Dialog> buildDialogsList(boolean isShared) {
        im.actor.runtime.Runtime.checkMainThread();

        DisplayList<Dialog> res = Storage.createDisplayList(context().getMessagesModule().getDialogsEngine(),
                isShared, Dialog.ENTITY_NAME);

        res.initTop();

        return res;

//        ListEngine<Dialog> dialogsEngine = context().getMessagesModule().getDialogsEngine();
//        if (!(dialogsEngine instanceof ListEngineDisplayExt)) {
//            throw new RuntimeException("Dialogs ListEngine must implement ListEngineDisplayExt for using global list");
//        }
//
//        BindedDisplayList.BindHook<Dialog> hook = null;
//        if (isGlobalList) {
//            hook = new BindedDisplayList.BindHook<Dialog>() {
//
//                @Override
//                public void onScrolledToEnd() {
//                    context().getMessagesModule().loadMoreDialogs();
//                }
//
//                @Override
//                public void onItemTouched(Dialog item) {
//
//                }
//            };
//        }
//        BindedDisplayList<Dialog> displayList = new BindedDisplayList<Dialog>((ListEngineDisplayExt<Dialog>) dialogsEngine,
//                isGlobalList, LOAD_PAGE, LOAD_GAP, operationMode, hook);
//        displayList.initTop(false);
//        return displayList;
    }

    public DisplayList<Contact> buildContactList(boolean isShared) {
        im.actor.runtime.Runtime.checkMainThread();

        DisplayList<Contact> res = Storage.createDisplayList(context().getContactsModule().getContacts(),
                isShared, Contact.ENTITY_NAME);
        res.initTop();
        return res;
    }

    public DisplayList<Message> buildChatList(final Peer peer, boolean isShared) {
        im.actor.runtime.Runtime.checkMainThread();

        DisplayList<Message> res = Storage.createDisplayList(context().getMessagesModule().getConversationEngine(peer),
                isShared, Message.ENTITY_NAME);

        long lastRead = context().getMessagesModule().loadReadState(peer);

        if (lastRead != 0) {
            res.initCenter(lastRead);
        } else {
            res.initTop();
        }

        return res;
//
//        if (lastRead != 0)
//            chatList.initCenter(lastRead, false);
//        else
//            chatList.initTop(false);

//        ListEngine<Message> messagesEngine = context().getMessagesModule().getConversationEngine(peer);
//        if (!(messagesEngine instanceof ListEngineDisplayExt)) {
//            throw new RuntimeException("Conversation ListEngine must implement ListEngineDisplayExt for using global list");
//        }
//
//        BindedDisplayList.BindHook<Message> hook = null;
//        if (isGlobalList) {
//            hook = new BindedDisplayList.BindHook<Message>() {
//
//                @Override
//                public void onScrolledToEnd() {
//                    context().getMessagesModule().loadMoreHistory(peer);
//                }
//
//                @Override
//                public void onItemTouched(Message item) {
//                    if (item.isOnServer()) {
//                        context().getMessagesModule().onMessageShown(peer, item.getSortDate());
//                    }
//                }
//            };
//        }
//
//        // BaseAsyncStorageProvider storageProvider = (BaseAsyncStorageProvider) modules().getConfiguration().getStorageProvider();
//
//        BindedDisplayList<Message> chatList = new BindedDisplayList<Message>((ListEngineDisplayExt<Message>) messagesEngine,
//                isGlobalList, 20, 20, operationMode, hook);
//
//        long lastRead = context().getMessagesModule().loadReadState(peer);
//
//        if (lastRead != 0)
//            chatList.initCenter(lastRead, false);
//        else
//            chatList.initTop(false);
//        return chatList;
    }


    public DisplayList<SearchEntity> buildSearchList(boolean isGlobalList) {
        im.actor.runtime.Runtime.checkMainThread();

        DisplayList<SearchEntity> res = Storage.createDisplayList(context().getSearchModule().getSearchList(),
                isGlobalList, SearchEntity.ENTITY_NAME);
        res.initEmpty();
        return res;
    }
}
