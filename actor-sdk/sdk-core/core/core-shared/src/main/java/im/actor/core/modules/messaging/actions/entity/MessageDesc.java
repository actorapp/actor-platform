package im.actor.core.modules.messaging.actions.entity;

public class MessageDesc {

    private long rid;
    private boolean isOut;
    private long date;
    private int timer;
    private boolean isNeedExplicitRead;

    public MessageDesc(long rid, boolean isOut, long date, int timer, boolean isNeedExplicitRead) {
        this.rid = rid;
        this.isOut = isOut;
        this.date = date;
        this.timer = timer;
        this.isNeedExplicitRead = isNeedExplicitRead;
    }

    public long getRid() {
        return rid;
    }

    public boolean isOut() {
        return isOut;
    }

    public long getDate() {
        return date;
    }

    public int getTimer() {
        return timer;
    }

    public boolean isNeedExplicitRead() {
        return isNeedExplicitRead;
    }
}
