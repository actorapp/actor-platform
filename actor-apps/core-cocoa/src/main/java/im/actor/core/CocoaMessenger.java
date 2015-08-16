package im.actor.core;

import com.google.j2objc.annotations.ObjectiveCName;

import org.jetbrains.annotations.NotNull;

import java.util.HashMap;

import im.actor.core.entity.Contact;
import im.actor.core.entity.Dialog;
import im.actor.core.entity.Message;
import im.actor.core.entity.Peer;
import im.actor.core.entity.SearchEntity;
import im.actor.runtime.generic.mvvm.BindedDisplayList;

public class CocoaMessenger extends Messenger {

    private BindedDisplayList<Dialog> dialogList;
    private HashMap<Peer, BindedDisplayList<Message>> messagesLists = new HashMap<Peer, BindedDisplayList<Message>>();

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
        if (dialogList == null) {
            dialogList = (BindedDisplayList<Dialog>) modules.getDisplayListsModule().getDialogsSharedList();
            dialogList.setBindHook(new BindedDisplayList.BindHook<Dialog>() {
                @Override
                public void onScrolledToEnd() {
                    modules.getMessagesModule().loadMoreDialogs();
                }

                @Override
                public void onItemTouched(Dialog item) {

                }
            });
        }

        return dialogList;
    }

    @ObjectiveCName("getMessageDisplayList:")
    public BindedDisplayList<Message> getMessageDisplayList(final Peer peer) {
        if (!messagesLists.containsKey(peer)) {
            BindedDisplayList<Message> list = (BindedDisplayList<Message>) modules.getDisplayListsModule().getMessagesSharedList(peer);
            list.setBindHook(new BindedDisplayList.BindHook<Message>() {
                @Override
                public void onScrolledToEnd() {
                    modules.getMessagesModule().loadMoreHistory(peer);
                }

                @Override
                public void onItemTouched(Message item) {
                    if (item.isOnServer()) {
                        modules.getMessagesModule().onMessageShown(peer, item.getSenderId(), item.getSortDate());
                    }
                }
            });
            messagesLists.put(peer, list);
        }

        return messagesLists.get(peer);
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
