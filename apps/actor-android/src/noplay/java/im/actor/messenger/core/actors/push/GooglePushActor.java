package im.actor.messenger.core.actors.push;

import com.droidkit.actors.Actor;
import com.droidkit.actors.ActorRef;
import com.droidkit.actors.ActorSystem;
import com.droidkit.actors.Props;

/**
 * Created by ex3ndr on 18.09.14.
 */
public class GooglePushActor extends Actor {
    public static ActorRef push() {
        return ActorSystem.system().actorOf(Props.create(GooglePushActor.class).changeDispatcher("im/actor/messenger/core/actors/push"), "google_push");
    }
}
