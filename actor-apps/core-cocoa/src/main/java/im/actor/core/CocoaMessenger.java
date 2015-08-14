package im.actor.core;

import com.google.j2objc.annotations.ObjectiveCName;

import org.jetbrains.annotations.NotNull;

import im.actor.core.entity.Contact;
import im.actor.core.entity.Dialog;
import im.actor.core.entity.Message;
import im.actor.core.entity.Peer;
import im.actor.core.entity.SearchEntity;
import im.actor.runtime.generic.mvvm.BindedDisplayList;

public class CocoaMessenger extends Messenger {

    /**
     * Construct messenger
     *
     * @param configuration configuration of messenger
     */
    @ObjectiveCName("initWithConfiguration:")
    public CocoaMessenger(@NotNull Configuration configuration) {
        super(configuration);
    }

    @ObjectiveCName("getDialogsDisplayList")
    public BindedDisplayList<Dialog> getDialogsDisplayList() {
        return (BindedDisplayList<Dialog>) modules.getDisplayListsModule().getDialogsSharedList();
    }

    @ObjectiveCName("getMessageDisplayList:")
    public BindedDisplayList<Message> getMessageDisplayList(Peer peer) {
        return (BindedDisplayList<Message>) modules.getDisplayListsModule().getMessagesSharedList(peer);
    }

    @ObjectiveCName("buildSearchDisplayList")
    public BindedDisplayList<SearchEntity> buildSearchDisplayList() {
        return (BindedDisplayList<SearchEntity>) modules.getDisplayListsModule().buildSearchList(false);
    }

    @ObjectiveCName("buildContactsDisplayList")
    public BindedDisplayList<Contact> buildContactsDisplayList() {
        return (BindedDisplayList<Contact>) modules.getDisplayListsModule().buildContactList(false);
    }
}
