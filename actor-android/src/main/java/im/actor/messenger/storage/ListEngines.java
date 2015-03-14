package im.actor.messenger.storage;

import im.actor.model.entity.Contact;
import im.actor.model.entity.Message;
import im.actor.model.entity.Peer;
import im.actor.model.mvvm.BindedDisplayList;

public final class ListEngines {

    private ListEngines() {
    }

    public static BindedDisplayList<Contact> getContactsUiListEngine() {
        return null;
    }

    public static BindedDisplayList<Message> getMessagesList(Peer peer) {
        return null;
    }
}