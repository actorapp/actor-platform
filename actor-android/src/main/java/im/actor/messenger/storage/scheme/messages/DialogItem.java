package im.actor.messenger.storage.scheme.messages;

import com.droidkit.bser.BserObject;
import com.droidkit.bser.BserValues;
import com.droidkit.bser.BserWriter;
import com.droidkit.engine.list.ListItemIdentity;

import im.actor.messenger.model.DialogUids;
import im.actor.messenger.storage.scheme.avatar.Avatar;

import java.io.IOException;

/**
 * Created by ex3ndr on 18.10.14.
 */
public class DialogItem extends BserObject implements ListItemIdentity {

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

    public DialogItem(int type, int id, String dialogTitle, int unreadCount, long rid, long sortKey, int senderId,
                      long time, int messageType, String text, MessageState status, Avatar avatar, int relatedUid) {
        this.type = type;
        this.id = id;
        this.dialogTitle = dialogTitle;
        this.unreadCount = unreadCount;
        this.rid = rid;
        this.sortKey = sortKey;
        this.senderId = senderId;
        this.time = time;
        this.messageType = messageType;
        this.text = text;
        this.status = status;
        this.avatar = avatar;
        this.relatedUid = relatedUid;
    }

    public DialogItem() {
    }

    public int getType() {
        return type;
    }

    public int getId() {
        return id;
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

    public int getMessageType() {
        return messageType;
    }

    public String getText() {
        return text;
    }

    public MessageState getStatus() {
        return status;
    }

    public Avatar getAvatar() {
        return avatar;
    }

    public int getRelatedUid() {
        return relatedUid;
    }

    // List implementations

    @Override
    public long getListId() {
        return DialogUids.getDialogUid(type, id);
    }

    @Override
    public long getListSortKey() {
        return sortKey;
    }

    // Serialization logic

    private static final int FIELD_CHAT_TYPE = 1;
    private static final int FIELD_CHAT_ID = 2;
    private static final int FIELD_TITLE = 5;
    private static final int FIELD_UNREAD = 6;
    private static final int FIELD_RID = 3;
    private static final int FIELD_SORTKEY = 4;
    private static final int FIELD_SENDER_ID = 7;
    private static final int FIELD_TIME = 10;
    private static final int FIELD_TYPE = 8;
    private static final int FIELD_TEXT = 9; // Optional
    private static final int FIELD_STATUS = 11;
    private static final int FIELD_AVATAR = 12; // Optional
    private static final int FIELD_RELATED = 13; // Optional

    @Override
    public void parse(BserValues reader) throws IOException {
        type = reader.getInt(FIELD_CHAT_TYPE);
        id = reader.getInt(FIELD_CHAT_ID);
        dialogTitle = reader.getString(FIELD_TITLE);
        unreadCount = reader.getInt(FIELD_UNREAD);
        rid = reader.getLong(FIELD_RID);
        sortKey = reader.getLong(FIELD_SORTKEY);
        senderId = reader.getInt(FIELD_SENDER_ID);
        time = reader.getLong(FIELD_TIME);
        messageType = reader.getInt(FIELD_TYPE);
        text = reader.optString(FIELD_TEXT);

        status = MessageState.parse(reader.getInt(FIELD_STATUS));

        avatar = reader.optObj(FIELD_AVATAR, Avatar.class);

        relatedUid = reader.optInt(FIELD_RELATED);
    }

    @Override
    public void serialize(BserWriter writer) throws IOException {
        writer.writeInt(FIELD_CHAT_TYPE, type);
        writer.writeInt(FIELD_CHAT_ID, id);
        writer.writeString(FIELD_TITLE, dialogTitle);
        writer.writeInt(FIELD_UNREAD, unreadCount);

        writer.writeLong(FIELD_RID, rid);
        writer.writeLong(FIELD_SORTKEY, sortKey);
        writer.writeInt(FIELD_SENDER_ID, senderId);
        writer.writeLong(FIELD_TIME, time);
        writer.writeInt(FIELD_TYPE, messageType);
        if (text != null) {
            writer.writeString(FIELD_TEXT, text);
        }

        writer.writeInt(FIELD_STATUS, MessageState.serialize(status));
        if (avatar != null) {
            writer.writeObject(FIELD_AVATAR, avatar);
        }

        writer.writeInt(FIELD_RELATED, relatedUid);
    }
}
