package com.droidkit.actors;

import com.droidkit.actors.mailbox.ActorDispatcher;
import com.droidkit.actors.mailbox.ActorEndpoint;
import com.droidkit.actors.mailbox.Mailbox;

import java.util.UUID;

/**
 * <p>INTERNAL API</p>
 * Actor Scope contains UUID, Path, Props and Actor (if created).
 *
 * @author Steve Ex3NDR Korshakov (steve@actor.im)
 */
public class ActorScope {

    private final UUID uuid;
    private final String path;
    private final Props props;

    private final ActorRef actorRef;
    private final Mailbox mailbox;

    private final ActorDispatcher dispatcher;

    private final ActorSystem actorSystem;

    private Actor actor;

    private ActorRef sender;

    private ActorEndpoint endpoint;

    public ActorScope(ActorSystem actorSystem, Mailbox mailbox, ActorDispatcher dispatcher, UUID uuid, String path, Props props,
                      ActorEndpoint endpoint) {
        this.actorSystem = actorSystem;
        this.mailbox = mailbox;
        this.actorRef = new ActorRef(endpoint, actorSystem, dispatcher, uuid, path);
        this.dispatcher = dispatcher;
        this.uuid = uuid;
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

    public UUID getUuid() {
        return uuid;
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
