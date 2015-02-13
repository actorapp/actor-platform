package im.actor.messenger.core.actors.notifications;

import im.actor.messenger.storage.scheme.messages.ConversationMessage;

/**
 * Created by ex3ndr on 07.09.14.
 */
public interface NotificationsInt {

    public void onAppOpened();

    public void onAppClosed();

    public void onChatOpen(int type, int id);

    public void onChatClose(int type, int id);

    public void onNewMessage(int type, int id, ConversationMessage conversationMessage);

    public void toggleNotifications(int type, int id);
}
