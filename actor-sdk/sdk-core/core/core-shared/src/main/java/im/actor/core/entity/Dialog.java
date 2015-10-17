/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.core.entity;

import com.google.j2objc.annotations.Property;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;

import im.actor.runtime.bser.Bser;
import im.actor.runtime.bser.BserCreator;
import im.actor.runtime.bser.BserObject;
import im.actor.runtime.bser.BserValues;
import im.actor.runtime.bser.BserWriter;
import im.actor.runtime.storage.ListEngineItem;

public class Dialog extends BserObject implements ListEngineItem {

    public static Dialog fromBytes(byte[] date) throws IOException {
        return Bser.parse(new Dialog(), date);
    }

    public static BserCreator<Dialog> CREATOR = new BserCreator<Dialog>() {
        @Override
        public Dialog createInstance() {
            return new Dialog();
        }
    };

    public static final String ENTITY_NAME = "Dialog";

    @NotNull
    @SuppressWarnings("NullableProblems")
    @Property("readonly, nonatomic")
    private Peer peer;
    @NotNull
    @SuppressWarnings("NullableProblems")
    @Property("readonly, nonatomic")
    private String dialogTitle;
    @Property("readonly, nonatomic")
    private int unreadCount;
    @Property("readonly, nonatomic")
    private long rid;
    @Property("readonly, nonatomic")
    private long sortDate;
    @Property("readonly, nonatomic")
    private int senderId;
    @Property("readonly, nonatomic")
    private long date;
    @NotNull
    @SuppressWarnings("NullableProblems")
    @Property("readonly, nonatomic")
    private ContentType messageType;
    @NotNull
    @SuppressWarnings("NullableProblems")
    @Property("readonly, nonatomic")
    private String text;
    @NotNull
    @SuppressWarnings("NullableProblems")
    @Property("readonly, nonatomic")
    private MessageState status;
    @Nullable
    @Property("readonly, nonatomic")
    private Avatar dialogAvatar;
    @Property("readonly, nonatomic")
    private int relatedUid;

    public Dialog(@NotNull Peer peer,
                  long sortKey,
                  @NotNull String dialogTitle,
                  @Nullable Avatar dialogAvatar,
                  int unreadCount,
                  long rid,
                  @NotNull ContentType messageType,
                  @NotNull String text,
                  @NotNull MessageState status,
                  int senderId,
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

    @NotNull
    public Peer getPeer() {
        return peer;
    }

    @NotNull
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

    @NotNull
    public ContentType getMessageType() {
        return messageType;
    }

    @NotNull
    public String getText() {
        return text;
    }

    @NotNull
    public MessageState getStatus() {
        return status;
    }

    public int getRelatedUid() {
        return relatedUid;
    }

    @Nullable
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
            dialogAvatar = new Avatar(av);
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

    @Override
    public long getEngineId() {
        return peer.getUnuqueId();
    }

    @Override
    public long getEngineSort() {
        return sortDate;
    }

    @Override
    public String getEngineSearch() {
        return dialogTitle;
    }
}
