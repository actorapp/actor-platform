package im.actor.core.modules.users.router.entity;

import im.actor.runtime.actors.ask.AskMessage;
import im.actor.runtime.actors.messages.Void;

public class RouterLocalNameChanged implements AskMessage<Void> {

    private int uid;
    private String localName;

    public RouterLocalNameChanged(int uid, String localName) {
        this.uid = uid;
        this.localName = localName;
    }

    public int getUid() {
        return uid;
    }

    public String getLocalName() {
        return localName;
    }
}
