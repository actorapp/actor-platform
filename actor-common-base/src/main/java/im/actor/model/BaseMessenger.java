package im.actor.model;

import im.actor.model.Configuration;
import im.actor.model.Messenger;
import im.actor.model.entity.Contact;
import im.actor.model.entity.Dialog;
import im.actor.model.entity.Message;
import im.actor.model.entity.Peer;
import im.actor.model.modules.DisplayLists;
import im.actor.model.mvvm.BindedDisplayList;
import im.actor.model.viewmodel.ConversationVM;
import im.actor.model.viewmodel.ConversationVMCallback;

/**
 * Created by ex3ndr on 27.03.15.
 */
public class BaseMessenger extends Messenger {

    private DisplayLists displayLists;

    public BaseMessenger(Configuration configuration) {
        super(configuration);
        displayLists = new DisplayLists(modules);
    }

    public ConversationVM buildConversationVM(Peer peer, ConversationVMCallback callback) {
        return new ConversationVM(peer, callback, modules, displayLists);
    }

    // Display lists

    public BindedDisplayList<Dialog> getDialogsGlobalList() {
        return displayLists.getDialogsGlobalList();
    }

    public BindedDisplayList<Message> getMessagesGlobalList(Peer peer) {
        return displayLists.getMessagesGlobalList(peer);
    }

    public BindedDisplayList<Contact> getContactsGlobalList() {
        return displayLists.getContactsGlobalList();
    }

    public BindedDisplayList<Contact> buildContactDisplayList() {
        return displayLists.buildNewContactList(false);
    }
}
