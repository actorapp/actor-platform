/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.model;

import im.actor.model.droidkit.actors.ActorSystem;
import im.actor.model.entity.Contact;
import im.actor.model.entity.Dialog;
import im.actor.model.entity.Message;
import im.actor.model.entity.Peer;
import im.actor.model.entity.SearchEntity;
import im.actor.model.modules.DisplayLists;
import im.actor.model.mvvm.BindedDisplayList;
import im.actor.model.viewmodel.ConversationVM;
import im.actor.model.viewmodel.ConversationVMCallback;

/**
 * Base messenger for asynchronous environments
 */
public class BaseMessenger extends Messenger {

    private DisplayLists displayLists;

    public BaseMessenger(Configuration configuration) {
        super(configuration);
        // Adding dispatcher for database
        ActorSystem.system().addDispatcher("db", 1);
        displayLists = new DisplayLists(modules);
    }

    public ConversationVM buildConversationVM(Peer peer, BindedDisplayList<Message> displayList,
                                              ConversationVMCallback callback) {
        return new ConversationVM(peer, callback, modules, displayList);
    }

    // Display lists

    public BindedDisplayList<Dialog> getDialogsGlobalList() {
        return displayLists.getDialogsGlobalList();
    }

    public BindedDisplayList<Message> getMessagesGlobalList(Peer peer) {
        return displayLists.getMessagesGlobalList(peer);
    }

    public BindedDisplayList<Message> buildMessagesList(Peer peer) {
        return displayLists.buildNewChatList(peer, false);
    }

    public BindedDisplayList<Message> getMediaGlobalList(Peer peer) {
        return displayLists.getMessagesMediaList(peer);
    }

    public int getMediaCount(Peer peer) {
        return displayLists.getMediaCount(peer);
    }

    public BindedDisplayList<Contact> getContactsGlobalList() {
        return displayLists.getContactsGlobalList();
    }

    public BindedDisplayList<Contact> buildContactDisplayList() {
        return displayLists.buildNewContactList(false);
    }

    public BindedDisplayList<SearchEntity> buildSearchList() {
        return displayLists.buildNewSearchList(false);
    }
}
