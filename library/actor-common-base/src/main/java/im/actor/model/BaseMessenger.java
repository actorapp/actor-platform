/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.model;

import com.google.j2objc.annotations.ObjectiveCName;

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
    private MessengerEnvironment environment;

    @ObjectiveCName("initWithEnvironment:withConfiguration:")
    public BaseMessenger(MessengerEnvironment environment, Configuration configuration) {
        super(configuration);
        this.environment = environment;
        displayLists = new DisplayLists(environment, modules);
    }

    @ObjectiveCName("buildConversationVMWithPeer:withDisplayList:withCallback:")
    public ConversationVM buildConversationVM(Peer peer, BindedDisplayList<Message> displayList,
                                              ConversationVMCallback callback) {
        return new ConversationVM(peer, callback, modules, displayList);
    }

    // Display lists

    @ObjectiveCName("getDialogsGlobalList")
    public BindedDisplayList<Dialog> getDialogsGlobalList() {
        return displayLists.getDialogsGlobalList();
    }

    @ObjectiveCName("getMessagesGlobalListWithPeer:")
    public BindedDisplayList<Message> getMessagesGlobalList(Peer peer) {
        return displayLists.getMessagesGlobalList(peer);
    }

    @ObjectiveCName("buildMessagesListWithPeer:")
    public BindedDisplayList<Message> buildMessagesList(Peer peer) {
        return displayLists.buildNewChatList(peer, false);
    }

    @ObjectiveCName("getMediaGlobalListWithPeer:")
    public BindedDisplayList<Message> getMediaGlobalList(Peer peer) {
        return displayLists.getMessagesMediaList(peer);
    }

    @ObjectiveCName("getMediaCountWithPeer:")
    public int getMediaCount(Peer peer) {
        return displayLists.getMediaCount(peer);
    }

    @ObjectiveCName("getContactsGlobalListWithContact")
    public BindedDisplayList<Contact> getContactsGlobalList() {
        return displayLists.getContactsGlobalList();
    }

    @ObjectiveCName("buildContactDisplayList")
    public BindedDisplayList<Contact> buildContactDisplayList() {
        return displayLists.buildNewContactList(false);
    }

    @ObjectiveCName("buildSearchList")
    public BindedDisplayList<SearchEntity> buildSearchList() {
        return displayLists.buildNewSearchList(false);
    }

    @ObjectiveCName("loadLastReadState:")
    public long loadLastReadState(Peer peer){
        return modules.getMessagesModule().loadReadState(peer);
    }
}
