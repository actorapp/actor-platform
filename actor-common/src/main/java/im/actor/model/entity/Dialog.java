package im.actor.model.entity;

import im.actor.model.droidkit.bser.Bser;
import im.actor.model.droidkit.bser.BserObject;
import im.actor.model.droidkit.bser.BserValues;
import im.actor.model.droidkit.bser.BserWriter;
import im.actor.model.mvvm.ListEngineItem;

import java.io.IOException;

/**
 * Created by ex3ndr on 09.02.15.
 */
public class Dialog extends BserObject implements ListEngineItem {

    public static Dialog fromBytes(byte[] date) throws IOException {
        return Bser.parse(new Dialog(), date);
    }

    public enum ContentType {
        TEXT, EMPTY
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
        switch (values.getInt(9)) {
            default:
            case 1:
                messageType = ContentType.EMPTY;
                break;
            case 2:
                messageType = ContentType.TEXT;
                break;
        }
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
        switch (messageType) {
            default:
            case EMPTY:
                writer.writeInt(9, 1);
                break;
            case TEXT:
                writer.writeInt(9, 2);
                break;
        }
        writer.writeString(10, text);
        writer.writeInt(11, status.getValue());
        writer.writeInt(12, relatedUid);
    }
}
