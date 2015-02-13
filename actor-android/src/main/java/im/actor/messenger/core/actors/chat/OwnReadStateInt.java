package im.actor.messenger.core.actors.chat;

/**
 * Created by ex3ndr on 25.10.14.
 */
public interface OwnReadStateInt {

    public void newOutMessage(int chatType, int chatId, long rid, long sortingKey, long date,
                              boolean isEncrypted);

    public void newMessage(int chatType, int chatId, long rid, long sortingKey, long date,
                           boolean isEncrypted);

    public void messageRead(int chatType, int chatId, long rid, long sortingKey, long date,
                            boolean isEncrypted);

    public void messagePlainReadByMe(int chatType, int chatId, long date);

    public void messageEncryptedReadByMe(int chatType, int chatId, long rid);
}
