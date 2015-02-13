package im.actor.messenger.core.actors.chat;

/**
 * Created by ex3ndr on 25.11.14.
 */
public interface ChatActionsInt {

    public void deleteMessages(int chatType, int chatId, long[] messages);

    public void clearChat(int chatType, int chatId);

    public void deleteChat(int chatType, int chatId);
}
