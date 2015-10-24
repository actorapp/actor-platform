package im.actor;

import java.util.Optional;

import im.actor.botkit.RemoteBot;
import im.actor.bots.BotMessages;
import shardakka.ShardakkaExtension;
import shardakka.keyvalue.SimpleKeyValueJava;

/**
 * Example Hello Bot: Entry point of development bots for Actor Platform
 */
public class HelloBot extends RemoteBot {

    /**
     * Local persistent key-value. Useful for storing bot's data.
     */
    private SimpleKeyValueJava<String> localKeyValue;

    public HelloBot(String token, String endpoint) {
        super(token, endpoint);

        // Creating KeyValue. Don't try to understand this, this is not necessary.
        // We will improve this in future versions
        // "msgs" is name of storage, asJava is required for better Java API.
        localKeyValue = ShardakkaExtension.get(context().system()).simpleKeyValue("msgs").asJava();
    }


    @Override
    public void onMessage(BotMessages.Message message) {
        if (message.message() instanceof BotMessages.TextMessage) {
            // If Message is Text Message
            BotMessages.TextMessage text = (BotMessages.TextMessage) message.message();


            if (text.text().trim().toLowerCase().startsWith("hello")) {
                // If Message starts from hello: Sending response

                // Get User of sender
                BotMessages.User user = getUser(message.sender().id());

                // Sending welcome message
                requestSendMessage(message.peer(),
                        nextRandomId(), new BotMessages.TextMessage("Hi, " + user.name() + "!"));
            } else if (text.text().trim().toLowerCase().startsWith("last")) {
                // If Message starts from last: Sending last sent message

                // Reading last_message from disc
                Optional<String> res = localKeyValue.syncGet("last_message");

                if (res.isPresent()) {

                    // If present: send it back
                    requestSendMessage(message.peer(),
                            nextRandomId(), new BotMessages.TextMessage("Last message:  " + res.get()));
                } else {

                    requestSendMessage(message.peer(),
                            nextRandomId(), new BotMessages.TextMessage("I'm alone :'("));
                }
            } else {

                // Else ask person about saying hello
                requestSendMessage(message.peer(), nextRandomId(), new BotMessages.TextMessage("Please, say hello"));
            }

            // Store last message
            localKeyValue.syncUpsert("last_message", text.text());
        }
    }
}