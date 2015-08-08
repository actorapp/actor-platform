/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.runtime.actors;

import im.actor.runtime.actors.mailbox.ActorDispatcher;
import im.actor.runtime.actors.mailbox.ActorEndpoint;
import im.actor.runtime.actors.mailbox.Mailbox;

/**
 * <p>INTERNAL API</p>
 * Actor Scope contains UUID, Path, Props and Actor (if created).
 */
public class ActorScope {

    private final String path;
    private final Props props;

    private final ActorRef actorRef;
    private final Mailbox mailbox;

    private final ActorDispatcher dispatcher;

    private final ActorSystem actorSystem;

    private Actor actor;

    private ActorRef sender;

    private ActorEndpoint endpoint;

    public ActorScope(ActorSystem actorSystem, Mailbox mailbox, ActorDispatcher dispatcher, String path, Props props,
                      ActorEndpoint endpoint) {
        this.actorSystem = actorSystem;
        this.mailbox = mailbox;
        this.actorRef = new ActorRef(endpoint, actorSystem, dispatcher, path);
        this.dispatcher = dispatcher;
        this.path = path;
        this.props = props;
        this.endpoint = endpoint;
    }

    public ActorEndpoint getEndpoint() {
        return endpoint;
    }

    public ActorDispatcher getDispatcher() {
        return dispatcher;
    }

    public String getPath() {
        return path;
    }

    public Props getProps() {
        return props;
    }

    public ActorRef getActorRef() {
        return actorRef;
    }

    public Mailbox getMailbox() {
        return mailbox;
    }

    public Actor getActor() {
        return actor;
    }

    public ActorSystem getActorSystem() {
        return actorSystem;
    }

    public ActorRef getSender() {
        return sender;
    }

    public void setSender(ActorRef sender) {
        this.sender = sender;
    }

    public void onActorCreated(Actor actor) {
        this.actor = actor;
    }

    public void onActorDie() {
        this.actor = null;
    }
}
