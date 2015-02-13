package im.actor.messenger.storage.scheme.messages;

import com.droidkit.bser.BserCompositeFieldDescription;
import com.droidkit.bser.BserObject;
import com.droidkit.bser.BserValues;
import com.droidkit.bser.BserWriter;
import com.droidkit.engine.list.ListItemIdentity;

import java.io.IOException;

import im.actor.messenger.storage.scheme.messages.types.AbsMessage;
import im.actor.messenger.storage.scheme.messages.types.AudioMessage;
import im.actor.messenger.storage.scheme.messages.types.DocumentMessage;
import im.actor.messenger.storage.scheme.messages.types.GroupAdd;
import im.actor.messenger.storage.scheme.messages.types.GroupAvatar;
import im.actor.messenger.storage.scheme.messages.types.GroupCreated;
import im.actor.messenger.storage.scheme.messages.types.GroupKick;
import im.actor.messenger.storage.scheme.messages.types.GroupLeave;
import im.actor.messenger.storage.scheme.messages.types.GroupTitle;
import im.actor.messenger.storage.scheme.messages.types.PhotoMessage;
import im.actor.messenger.storage.scheme.messages.types.TextMessage;
import im.actor.messenger.storage.scheme.messages.types.UserAddedDeviceMessage;
import im.actor.messenger.storage.scheme.messages.types.UserRegisteredMessage;
import im.actor.messenger.storage.scheme.messages.types.VideoMessage;

/**
 * Created by ex3ndr on 25.10.14.
 */
public class ConversationMessage extends BserObject implements ListItemIdentity {

    private static final BserCompositeFieldDescription<AbsMessage> MESSAGE_FIELD = new BserCompositeFieldDescription<AbsMessage>() {
        @Override
        protected void init() {
            registerClass(16, TextMessage.class);
            registerClass(17, PhotoMessage.class);
            registerClass(18, VideoMessage.class);
            registerClass(19, DocumentMessage.class);
            registerClass(20, AudioMessage.class);

            registerClass(21, UserRegisteredMessage.class);
            registerClass(22, UserAddedDeviceMessage.class);

            registerClass(23, GroupAdd.class);
            registerClass(24, GroupCreated.class);
            registerClass(25, GroupKick.class);
            registerClass(26, GroupLeave.class);
            registerClass(27, GroupTitle.class);
            registerClass(28, GroupAvatar.class);
        }
    };

    private long rid;
    private long sortKey;
    private long time;
    private int senderId;
    private MessageState messageState;

    private AbsMessage content;

    public ConversationMessage(long rid, long sortKey, long time, int senderId, MessageState messageState, AbsMessage content) {
        this.rid = rid;
        this.sortKey = sortKey;
        this.time = time;
        this.senderId = senderId;
        this.messageState = messageState;
        this.content = content;
    }

    public ConversationMessage() {

    }

    public ConversationMessage changeState(MessageState messageState) {
        return new ConversationMessage(rid, sortKey, time, senderId, messageState, content);
    }

    public ConversationMessage changeContent(AbsMessage content) {
        return new ConversationMessage(rid, sortKey, time, senderId, messageState, content);
    }

    public ConversationMessage changeDate(long time) {
        return new ConversationMessage(rid, sortKey, time, senderId, messageState, content);
    }

    public long getRid() {
        return rid;
    }

    public long getSortKey() {
        return sortKey;
    }

    public long getTime() {
        return time;
    }

    public int getSenderId() {
        return senderId;
    }

    public MessageState getMessageState() {
        return messageState;
    }

    public AbsMessage getContent() {
        return content;
    }

    @Override
    public void parse(BserValues values) throws IOException {
        rid = values.getLong(1);
        sortKey = values.getLong(2);
        time = values.getLong(3);
        senderId = values.getInt(4);
        messageState = MessageState.parse(values.getInt(5));
        content = values.getComposite(MESSAGE_FIELD);
    }

    @Override
    public void serialize(BserWriter writer) throws IOException {
        writer.writeLong(1, rid);
        writer.writeLong(2, sortKey);
        writer.writeLong(3, time);
        writer.writeInt(4, senderId);
        writer.writeInt(5, MessageState.serialize(messageState));
        writer.writeComposite(MESSAGE_FIELD, content);
    }

    @Override
    public long getListId() {
        return rid;
    }

    @Override
    public long getListSortKey() {
        return sortKey;
    }
}
