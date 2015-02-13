package im.actor.messenger.core.actors.messages;

/**
 * Created by ex3ndr on 16.11.14.
 */
public interface PlainReceivedInt {

    public void markReceived(int chatType, int chatId, long date);
}
