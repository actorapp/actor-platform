package im.actor.core.modules.users.router.entity;

import im.actor.runtime.actors.ask.AskMessage;
import im.actor.runtime.actors.messages.Void;

public class RouterNameChanged implements AskMessage<Void> {

    private int uid;
    private String name;

    public RouterNameChanged(int uid, String name) {
        this.uid = uid;
        this.name = name;
    }

    public int getUid() {
        return uid;
    }

    public String getName() {
        return name;
    }
}
