/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.core.entity;

public class DialogBuilder {
    private Peer peer;
    private long sortKey;
    private String dialogTitle;
    private Avatar dialogAvatar;
    private int unreadCount;
    private long rid;
    private ContentType messageType;
    private String text;
    private MessageState status;
    private int senderId;
    private long time;
    private int relatedUid = 0;

    public DialogBuilder() {

    }

    public DialogBuilder(Dialog dialog) {
        peer = dialog.getPeer();
        sortKey = dialog.getSortDate();
        dialogTitle = dialog.getDialogTitle();
        dialogAvatar = dialog.getDialogAvatar();
        unreadCount = dialog.getUnreadCount();
        rid = dialog.getRid();
        messageType = dialog.getMessageType();
        text = dialog.getText();
        status = dialog.getStatus();
        senderId = dialog.getSenderId();
        time = dialog.getDate();
        relatedUid = dialog.getRelatedUid();
    }

    public DialogBuilder setPeer(Peer peer) {
        this.peer = peer;
        return this;
    }

    public DialogBuilder setSortKey(long sortKey) {
        this.sortKey = sortKey;
        return this;
    }

    public DialogBuilder setDialogTitle(String dialogTitle) {
        this.dialogTitle = dialogTitle;
        return this;
    }

    public DialogBuilder setUnreadCount(int unreadCount) {
        this.unreadCount = unreadCount;
        return this;
    }

    public DialogBuilder setRid(long rid) {
        this.rid = rid;
        return this;
    }

    public DialogBuilder setMessageType(ContentType messageType) {
        this.messageType = messageType;
        return this;
    }

    public DialogBuilder setText(String text) {
        this.text = text;
        return this;
    }

    public DialogBuilder setStatus(MessageState status) {
        this.status = status;
        return this;
    }

    public DialogBuilder setSenderId(int senderId) {
        this.senderId = senderId;
        return this;
    }

    public DialogBuilder setTime(long time) {
        this.time = time;
        return this;
    }

    public DialogBuilder setRelatedUid(int relatedUid) {
        this.relatedUid = relatedUid;
        return this;
    }

    public DialogBuilder setDialogAvatar(Avatar avatar) {
        this.dialogAvatar = avatar;
        return this;
    }

    public Dialog createDialog() {
        return new Dialog(peer, sortKey, dialogTitle, dialogAvatar, unreadCount, rid, messageType, text, status, senderId, time, relatedUid);
    }
}