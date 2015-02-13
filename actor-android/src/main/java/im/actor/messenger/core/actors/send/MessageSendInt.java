package im.actor.messenger.core.actors.send;

import im.actor.messenger.storage.scheme.FileLocation;

/**
 * Created by ex3ndr on 04.10.14.
 */
public interface MessageSendInt {
    public void sendText(int chatType, int chatId, String text, boolean isEncrypted);

    public void sendFile(int chatType, int chatId, long rid, FileLocation fileLocation,
                         String name, int extType, byte[] extension, byte[] thumb,
                         boolean isEncrypted);
}
