/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.runtime.actors;

/**
 * <p>INTERNAL API</p>
 * Context of actor
 */
public class ActorContext {
    private final ActorScope actorScope;

    /**
     * <p>INTERNAL API</p>
     * Creating of actor context
     *
     * @param scope actor scope
     */
    public ActorContext(ActorScope scope) {
        this.actorScope = scope;
    }

    /**
     * Actor Reference
     *
     * @return reference
     */
    public ActorRef getSelf() {
        return actorScope.getActorRef();
    }

    /**
     * Actor system
     *
     * @return Actor system
     */
    public ActorSystem getSystem() {
        return actorScope.getActorSystem();
    }


    /**
     * Sender of last received message
     *
     * @return sender's ActorRef
     */
    public ActorRef sender() {
        return actorScope.getSender();
    }

    public Object message() {
        return actorScope.getMessage();
    }

    /**
     * Setting sender
     *
     * @param ref sender's ActorRef
     */
    public void setSender(ActorRef ref) {
        actorScope.setSender(ref);
    }

    public void setMessage(Object message) {
        actorScope.setMessage(message);
    }

}
