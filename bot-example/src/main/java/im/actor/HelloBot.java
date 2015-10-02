package im.actor;

import im.actor.bot.BotMessages;
import im.actor.botkit.RemoteBot;

public class HelloBot extends RemoteBot {
    public HelloBot(String token, String endpoint) {
        super(token, endpoint);
    }

    @Override
    public void onTextMessage(BotMessages.TextMessage tm) {
        if (tm.text().startsWith("hello")) {
            sendTextMessage(outPeer(tm.sender()), "Hi there!");
        } else {
            sendTextMessage(outPeer(tm.sender()), "Please, say hello");
        }
    }
}