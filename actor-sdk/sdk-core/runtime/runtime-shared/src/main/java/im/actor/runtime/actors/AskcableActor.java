package im.actor.runtime.actors;

import im.actor.runtime.Log;
import im.actor.runtime.actors.ask.AskIntRequest;
import im.actor.runtime.promise.Promise;
import im.actor.runtime.promise.PromiseResolver;
import im.actor.runtime.promise.Promises;

public class AskcableActor extends Actor {

    public Promise onAsk(Object message) throws Exception {
        throw new RuntimeException("Not implemented");
    }

    @Override
    public void onReceive(Object message) {
        if (message instanceof AskIntRequest) {
            AskIntRequest askRequest = (AskIntRequest) message;
            try {
                Promise p = onAsk(askRequest.getMessage());
                if (p == null) {
                    // Just ignore. We assume that message is stashed
                    return;
                }
                p.pipeTo(askRequest.getFuture());
            } catch (Exception e) {
                e.printStackTrace();
                askRequest.getFuture().tryError(e);
            }
        } else {
            super.onReceive(message);
        }
    }
}
