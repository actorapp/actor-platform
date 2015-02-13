package im.actor.messenger.core.actors.chat;

/**
 * Created by ex3ndr on 19.11.14.
 */
public interface ReadStateInt {
    public void onNewOutMessage(int chatType, int chatId, long date, long rid, boolean isEncrypted);

    public void markMessagesRead(int chatType, int chatId, long date);

    public void markMessagesReceived(int chatType, int chatId, long date);

    public void markEncryptedRead(int chatType, int chatId, long rid);

    public void markEncryptedReceived(int chatType, int chatId, long rid);
}
