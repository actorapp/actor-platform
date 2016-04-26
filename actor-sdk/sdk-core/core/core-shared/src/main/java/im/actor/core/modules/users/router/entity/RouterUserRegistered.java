package im.actor.core.modules.users.router.entity;

import im.actor.runtime.actors.ask.AskMessage;
import im.actor.runtime.actors.messages.Void;

public class RouterUserRegistered implements AskMessage<Void> {

    private long rid;
    private int uid;
    private long date;

    public RouterUserRegistered(long rid, int uid, long date) {
        this.rid = rid;
        this.uid = uid;
        this.date = date;
    }

    public long getRid() {
        return rid;
    }

    public int getUid() {
        return uid;
    }

    public long getDate() {
        return date;
    }
}
