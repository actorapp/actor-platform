package im.actor.model.entity;

import im.actor.model.mvvm.ListEngineItem;

/**
 * Created by ex3ndr on 09.02.15.
 */
public class Dialog implements ListEngineItem {

    private final Peer peer;
    private final String dialogTitle;
    private final int unreadCount;
    private final long rid;
    private final long sortKey;
    private final int senderId;
    private final long time;
    private final ContentType messageType;
    private final String text;
    private final MessageState status;
    private Avatar dialogAvatar;
    private final int relatedUid;

    public Dialog(Peer peer,
                  long sortKey, String dialogTitle, Avatar dialogAvatar,
                  int unreadCount, long rid, ContentType messageType, String text,
                  MessageState status, int senderId,
                  long time,
                  int relatedUid) {
        this.peer = peer;
        this.dialogTitle = dialogTitle;
        this.dialogAvatar = dialogAvatar;
        this.unreadCount = unreadCount;
        this.rid = rid;
        this.sortKey = sortKey;
        this.senderId = senderId;
        this.time = time;
        this.messageType = messageType;
        this.text = text;
        this.status = status;
        this.relatedUid = relatedUid;
    }

    public Peer getPeer() {
        return peer;
    }

    @Override
    public long getListId() {
        return peer.getUid();
    }

    @Override
    public long getSortingKey() {
        return sortKey;
    }

    public String getDialogTitle() {
        return dialogTitle;
    }

    public int getUnreadCount() {
        return unreadCount;
    }

    public long getRid() {
        return rid;
    }

    public long getSortKey() {
        return sortKey;
    }

    public int getSenderId() {
        return senderId;
    }

    public long getTime() {
        return time;
    }

    public ContentType getMessageType() {
        return messageType;
    }

    public String getText() {
        return text;
    }

    public MessageState getStatus() {
        return status;
    }

    public int getRelatedUid() {
        return relatedUid;
    }

    public Avatar getDialogAvatar() {
        return dialogAvatar;
    }

    public Dialog editPeerInfo(String title, Avatar dialogAvatar) {
        return new Dialog(peer, sortKey, title, dialogAvatar, unreadCount, rid, messageType, text, status, senderId, time, relatedUid);
    }

    public enum ContentType {
        TEXT, EMPTY
    }
}
