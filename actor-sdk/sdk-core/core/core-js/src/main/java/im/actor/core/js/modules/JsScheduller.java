package im.actor.core.js.modules;

import im.actor.runtime.actors.Actor;
import im.actor.runtime.actors.ActorCreator;
import im.actor.runtime.actors.ActorRef;

import static im.actor.runtime.actors.ActorSystem.system;

public class JsScheduller {

    private static final ActorRef SCHEDULLER = system().actorOf("js/scheduller", new ActorCreator() {
        @Override
        public Actor create() {
            return new Actor();
        }
    });

    public static ActorRef scheduller() {
        return SCHEDULLER;
    }
}
