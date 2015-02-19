package im.actor.model.entity;

import im.actor.model.droidkit.bser.Bser;
import im.actor.model.droidkit.bser.BserObject;
import im.actor.model.droidkit.bser.BserValues;
import im.actor.model.droidkit.bser.BserWriter;
import im.actor.model.storage.ListEngineItem;

import java.io.IOException;

/**
 * Created by ex3ndr on 09.02.15.
 */
public class Dialog extends BserObject implements ListEngineItem {

    public static Dialog fromBytes(byte[] date) throws IOException {
        return Bser.parse(new Dialog(), date);
    }

    private Peer peer;
    private String dialogTitle;
    private int unreadCount;
    private long rid;
    private long sortDate;
    private int senderId;
    private long date;
    private ContentType messageType;
    private String text;
    private MessageState status;
    private Avatar dialogAvatar;
    private int relatedUid;

    public Dialog(Peer peer,
                  long sortKey, String dialogTitle, Avatar dialogAvatar,
                  int unreadCount, long rid, ContentType messageType, String text,
                  MessageState status, int senderId,
                  long date,
                  int relatedUid) {
        this.peer = peer;
        this.dialogTitle = dialogTitle;
        this.dialogAvatar = dialogAvatar;
        this.unreadCount = unreadCount;
        this.rid = rid;
        this.sortDate = sortKey;
        this.senderId = senderId;
        this.date = date;
        this.messageType = messageType;
        this.text = text;
        this.status = status;
        this.relatedUid = relatedUid;
    }

    private Dialog() {

    }

    public Peer getPeer() {
        return peer;
    }

    @Override
    public long getListId() {
        return peer.getUid();
    }

    @Override
    public long getListSortKey() {
        return sortDate;
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

    public long getSortDate() {
        return sortDate;
    }

    public int getSenderId() {
        return senderId;
    }

    public long getDate() {
        return date;
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
        return new Dialog(peer, sortDate, title, dialogAvatar, unreadCount, rid, messageType, text, status, senderId,
                date, relatedUid);
    }

    @Override
    public void parse(BserValues values) throws IOException {
        peer = Peer.fromBytes(values.getBytes(1));

        dialogTitle = values.getString(2);
        byte[] av = values.optBytes(3);
        if (av != null) {
            dialogAvatar = Avatar.fromBytes(av);
        }

        unreadCount = values.getInt(4);
        sortDate = values.getLong(5);

        rid = values.getLong(6);
        senderId = values.getInt(7);
        date = values.getLong(8);
        messageType = ContentType.fromValue(values.getInt(9));
        text = values.getString(10);
        status = MessageState.fromValue(values.getInt(11));
        relatedUid = values.getInt(12);
    }

    @Override
    public void serialize(BserWriter writer) throws IOException {
        writer.writeObject(1, peer);
        writer.writeString(2, dialogTitle);
        if (dialogAvatar != null) {
            writer.writeObject(3, dialogAvatar);
        }
        writer.writeInt(4, unreadCount);
        writer.writeLong(5, sortDate);
        writer.writeLong(6, rid);
        writer.writeInt(7, senderId);
        writer.writeLong(8, date);
        writer.writeInt(9, messageType.getValue());
        writer.writeString(10, text);
        writer.writeInt(11, status.getValue());
        writer.writeInt(12, relatedUid);
    }

    public enum ContentType {
        TEXT(2), EMPTY(1),
        DOCUMENT(3),
        DOCUMENT_PHOTO(4),
        DOCUMENT_VIDEO(5),
        SERVICE(6),
        SERVICE_ADD(7),
        SERVICE_KICK(8),
        SERVICE_LEAVE(9),
        SERVICE_REGISTERED(10),
        SERVICE_CREATED(11),
        SERVICE_TITLE(12),
        SERVICE_AVATAR(13),
        SERVICE_AVATAR_REMOVED(14);

        int value;

        ContentType(int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }

        public static ContentType fromValue(int value) {
            switch (value) {
                default:
                case 1:
                    return EMPTY;
                case 2:
                    return TEXT;
                case 3:
                    return DOCUMENT;
                case 4:
                    return DOCUMENT_PHOTO;
                case 5:
                    return DOCUMENT_VIDEO;
                case 6:
                    return SERVICE;
                case 7:
                    return SERVICE_ADD;
                case 8:
                    return SERVICE_KICK;
                case 9:
                    return SERVICE_LEAVE;
                case 10:
                    return SERVICE_REGISTERED;
                case 11:
                    return SERVICE_CREATED;
                case 12:
                    return SERVICE_TITLE;
                case 13:
                    return SERVICE_AVATAR;
                case 14:
                    return SERVICE_AVATAR_REMOVED;
            }
        }
    }

}
