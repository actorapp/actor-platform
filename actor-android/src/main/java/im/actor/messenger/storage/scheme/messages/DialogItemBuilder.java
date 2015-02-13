package im.actor.messenger.storage.scheme.messages;

import im.actor.messenger.storage.scheme.avatar.Avatar;

public class DialogItemBuilder {
    private int type;
    private int id;
    private String dialogTitle;
    private int unreadCount;
    private long rid;
    private long sortKey;
    private int senderId;
    private long time;
    private int messageType;
    private String text;
    private MessageState status;
    private Avatar avatar;
    private int relatedUid;

    public DialogItemBuilder() {

    }

    public DialogItemBuilder(DialogItem dialogItem) {
        type = dialogItem.getType();
        id = dialogItem.getId();
        dialogTitle = dialogItem.getDialogTitle();
        unreadCount = dialogItem.getUnreadCount();
        rid = dialogItem.getRid();
        sortKey = dialogItem.getSortKey();
        senderId = dialogItem.getSenderId();
        time = dialogItem.getTime();
        messageType = dialogItem.getMessageType();
        text = dialogItem.getText();
        status = dialogItem.getStatus();
        avatar = dialogItem.getAvatar();
        relatedUid = dialogItem.getRelatedUid();
    }

    public DialogItemBuilder setType(int type) {
        this.type = type;
        return this;
    }

    public DialogItemBuilder setId(int id) {
        this.id = id;
        return this;
    }

    public DialogItemBuilder setDialogTitle(String dialogTitle) {
        this.dialogTitle = dialogTitle;
        return this;
    }

    public DialogItemBuilder setUnreadCount(int unreadCount) {
        this.unreadCount = unreadCount;
        return this;
    }

    public DialogItemBuilder setRid(long rid) {
        this.rid = rid;
        return this;
    }

    public DialogItemBuilder setSortKey(long sortKey) {
        this.sortKey = sortKey;
        return this;
    }

    public DialogItemBuilder setSenderId(int senderId) {
        this.senderId = senderId;
        return this;
    }

    public DialogItemBuilder setTime(long time) {
        this.time = time;
        return this;
    }

    public DialogItemBuilder setMessageType(int messageType) {
        this.messageType = messageType;
        return this;
    }

    public DialogItemBuilder setText(String text) {
        this.text = text;
        return this;
    }

    public DialogItemBuilder setStatus(MessageState status) {
        this.status = status;
        return this;
    }

    public DialogItemBuilder setAvatar(Avatar avatar) {
        this.avatar = avatar;
        return this;
    }

    public DialogItemBuilder setRelatedUid(int relatedUid) {
        this.relatedUid = relatedUid;
        return this;
    }

    public DialogItem createDialogItem() {
        return new DialogItem(type, id, dialogTitle, unreadCount, rid, sortKey, senderId, time, messageType, text, status, avatar, relatedUid);
    }
}