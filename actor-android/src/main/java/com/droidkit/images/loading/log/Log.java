package com.droidkit.images.loading.log;

import com.droidkit.actors.Actor;
import com.droidkit.actors.ActorRef;
import com.droidkit.actors.ActorSystem;
import com.droidkit.actors.CurrentActor;

/**
 * Created by ex3ndr on 20.08.14.
 */
public class Log {
    private static final ActorRef ref = ActorSystem.system().actorOf(LogActor.class, "log");

    public static void d(ActorRef self, String log) {
        ref.send(log, self);
    }

    public static void d(String log) {
        Actor actor = CurrentActor.getCurrentActor();
        if (actor != null) {
            d(actor.self(), log);
        } else {
            ref.send(log);
        }
    }
}
