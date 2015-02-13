package im.actor.messenger.core.actors.notifications;

public class PendingNotificationBuilder {
    private int convType;
    private int convId;
    private long rid;
    private int uid;
    private long date;
    private PendingNotification.Type type;
    private int destUid;
    private String text;

    public PendingNotificationBuilder setConvType(int convType) {
        this.convType = convType;
        return this;
    }

    public PendingNotificationBuilder setConvId(int convId) {
        this.convId = convId;
        return this;
    }

    public PendingNotificationBuilder setRid(long rid) {
        this.rid = rid;
        return this;
    }

    public PendingNotificationBuilder setUid(int uid) {
        this.uid = uid;
        return this;
    }

    public PendingNotificationBuilder setDate(long date) {
        this.date = date;
        return this;
    }

    public PendingNotificationBuilder setType(PendingNotification.Type type) {
        this.type = type;
        return this;
    }

    public PendingNotificationBuilder setDestUid(int destUid) {
        this.destUid = destUid;
        return this;
    }

    public PendingNotificationBuilder setText(String text) {
        this.text = text;
        return this;
    }

    public PendingNotification createPendingNotification() {
        return new PendingNotification(convType, convId, rid, uid, date, type, text, destUid);
    }
}