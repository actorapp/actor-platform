package im.actor;

import akka.util.Timeout;
import im.actor.bot.BotMessages;
import im.actor.botkit.RemoteBot;
import scala.concurrent.Future;
import shardakka.ShardakkaExtension;
import shardakka.keyvalue.SimpleKeyValueJava;

import java.util.Optional;

import static scala.compat.java8.JFunction.proc;

public class HelloBot extends RemoteBot {

    SimpleKeyValueJava<String> msgsKv;
    Timeout timeout = new Timeout(1000);

    public HelloBot(String token, String endpoint) {
        super(token, endpoint);

        msgsKv = ShardakkaExtension.get(context().system()).simpleKeyValue("msgs", context().system()).asJava();
    }

    @Override
    public void onTextMessage(BotMessages.TextMessage tm) {
        if (tm.text().startsWith("hello")) {
            sendTextMessage(outPeer(tm.sender()), "Hi there!");
        } else if (tm.text().startsWith("last")) {
            Future<Optional<String>> future = msgsKv
                    .get("last", timeout);

            future.foreach(proc(s -> {
                String msg = s
                        .map(text -> "Last message I received was: " + text)
                        .orElse("I'm alone :'(");
                sendTextMessage(outPeer(tm.sender()), msg);
            }), context().dispatcher());
        } else {
            sendTextMessage(outPeer(tm.sender()), "Please, say hello");
        }

        msgsKv.upsert("last", tm.text(), timeout);
    }
}