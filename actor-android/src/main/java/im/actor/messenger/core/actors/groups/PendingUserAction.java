package im.actor.messenger.core.actors.groups;

import java.io.Serializable;

/**
 * Created by ex3ndr on 07.10.14.
 */
public class PendingUserAction implements Serializable {
    private int chatId;
    private long chatAccessHash;
    private int actionType;
    private int uid;
    private long userAccessHash;

    public PendingUserAction(int chatId, long chatAccessHash, int actionType, int uid, long userAccessHash) {
        this.chatId = chatId;
        this.actionType = actionType;
        this.uid = uid;
        this.chatAccessHash = chatAccessHash;
        this.userAccessHash = userAccessHash;
    }

    public int getChatId() {
        return chatId;
    }

    public int getActionType() {
        return actionType;
    }

    public int getUid() {
        return uid;
    }

    public long getUserAccessHash() {
        return userAccessHash;
    }

    public long getChatAccessHash() {
        return chatAccessHash;
    }

    public static final int ACTION_ADD = 0;
    public static final int ACTION_REMOVE = 1;
    public static final int ACTION_LEAVE = 2;
}
