package im.actor.core.modules.sequence;

import im.actor.runtime.actors.ActorRef;

public interface Processor {
    boolean process(ActorRef ref, Object update);
}
