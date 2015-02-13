package im.actor.messenger.core.actors.messages;

import java.io.Serializable;

/**
 * Created by ex3ndr on 08.10.14.
 */
public class MessageAction implements Serializable {
    private int chatType;
    private int chatId;
    private long rid;

    public MessageAction(int chatType, int chatId, long rid) {
        this.chatType = chatType;
        this.chatId = chatId;
        this.rid = rid;
    }

    public int getChatType() {
        return chatType;
    }

    public int getChatId() {
        return chatId;
    }

    public long getRid() {
        return rid;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        MessageAction that = (MessageAction) o;

        if (chatId != that.chatId) return false;
        if (chatType != that.chatType) return false;
        if (rid != that.rid) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = chatType;
        result = 31 * result + chatId;
        result = 31 * result + (int) (rid ^ (rid >>> 32));
        return result;
    }
}
