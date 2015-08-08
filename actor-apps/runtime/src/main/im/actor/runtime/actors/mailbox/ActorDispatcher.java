/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.runtime.actors.mailbox;

import java.util.HashMap;

import im.actor.runtime.actors.Actor;
import im.actor.runtime.actors.ActorContext;
import im.actor.runtime.actors.ActorRef;
import im.actor.runtime.actors.ActorScope;
import im.actor.runtime.actors.ActorSystem;
import im.actor.runtime.actors.ActorTime;
import im.actor.runtime.actors.Props;
import im.actor.runtime.actors.dispatch.AbstractDispatcher;
import im.actor.runtime.actors.messages.DeadLetter;
import im.actor.runtime.actors.messages.PoisonPill;
import im.actor.runtime.actors.messages.StartActor;

/**
 * Abstract Actor Dispatcher, used for dispatching messages for actors
 */
public abstract class ActorDispatcher {

    private final Object LOCK = new Object();
    private final HashMap<String, ActorEndpoint> endpoints = new HashMap<String, ActorEndpoint>();
    private final HashMap<String, ActorScope> scopes = new HashMap<String, ActorScope>();

    private final ActorSystem actorSystem;
    private String name;
    private AbstractDispatcher<Envelope, MailboxesQueue> dispatcher;

    public ActorDispatcher(String name, ActorSystem actorSystem) {
        this.name = name;
        this.actorSystem = actorSystem;
    }

    /**
     * Must be called in super constructor
     *
     * @param dispatcher thread dispatcher
     */
    protected void initDispatcher(AbstractDispatcher<Envelope, MailboxesQueue> dispatcher) {
        if (this.dispatcher != null) {
            throw new RuntimeException("Double dispatcher init");
        }
        this.dispatcher = dispatcher;
    }

    public final ActorRef referenceActor(String path, Props props) {
        synchronized (LOCK) {
            if (scopes.containsKey(path)) {
                return scopes.get(path).getActorRef();
            }

            Mailbox mailbox = props.createMailbox(dispatcher.getQueue());

            ActorEndpoint endpoint = endpoints.get(path);
            if (endpoint == null) {
                endpoint = new ActorEndpoint(path);
                endpoints.put(path, endpoint);
            }

            ActorScope scope = new ActorScope(actorSystem, mailbox, this, path, props, endpoint);
            endpoint.connect(mailbox, scope);
            scopes.put(scope.getPath(), scope);

            // Sending init message
            scope.getActorRef().send(StartActor.INSTANCE);
            return scope.getActorRef();
        }
    }

    public final void killGracefully(ActorScope scope) {
        scope.getActor().postStop();
        scope.onActorDie();

        for (Envelope e : scope.getMailbox().allEnvelopes()) {
            if (e.getSender() != null) {
                e.getSender().send(new DeadLetter(e.getMessage()));
            }
        }
        scope.getMailbox().clear();
        synchronized (LOCK) {
            scopes.remove(scope.getPath());
            endpoints.remove(scope.getPath());
            dispatcher.getQueue().disconnectMailbox(scope.getMailbox());
        }
    }

    private boolean isDisconnected(ActorEndpoint endpoint, Object message, ActorRef sender) {
        if (endpoint.isDisconnected()) {
            if (sender != null) {
                if (actorSystem.getTraceInterface() != null) {
                    actorSystem.getTraceInterface().onDeadLetter(sender, message);
                }
                sender.send(new DeadLetter(message));
            }
            return true;
        }
        return false;
    }

    public final void sendMessageAtTime(ActorEndpoint endpoint, Object message, long time, ActorRef sender) {
        if (!isDisconnected(endpoint, message, sender)) {
            endpoint.getMailbox().schedule(new Envelope(message, endpoint.getScope(), endpoint.getMailbox(), sender), time);
        }
    }

    public final void sendMessageNow(ActorEndpoint endpoint, Object message, ActorRef sender) {
        if (!isDisconnected(endpoint, message, sender)) {
            endpoint.getMailbox().schedule(new Envelope(message, endpoint.getScope(), endpoint.getMailbox(), sender), 0);
        }
    }

    public final void sendMessageOnceAtTime(ActorEndpoint endpoint, Object message, long time, ActorRef sender) {
        if (!isDisconnected(endpoint, message, sender)) {
            endpoint.getMailbox().scheduleOnce(new Envelope(message, endpoint.getScope(), endpoint.getMailbox(), sender), time);
        }
    }

    public final void sendMessageOnceNow(ActorEndpoint endpoint, Object message, ActorRef sender) {
        if (!isDisconnected(endpoint, message, sender)) {
            endpoint.getMailbox().scheduleOnce(new Envelope(message, endpoint.getScope(), endpoint.getMailbox(), sender), 0);
        }
    }

    public final void cancelSend(ActorEndpoint endpoint, Object message, ActorRef sender) {
        if (!endpoint.isDisconnected()) {
            endpoint.getMailbox().unschedule(new Envelope(message, endpoint.getScope(), endpoint.getMailbox(), sender));
        }
    }


    public String getName() {
        return name;
    }

    /**
     * Processing of envelope
     *
     * @param envelope envelope
     */
    protected void processEnvelope(Envelope envelope) {

        ActorScope scope = envelope.getScope();

        // Log.d("ACTOR_TRACE", "processEnvelope: " + envelope.getTopMessage() + " -> " + scope.getPath());

        if (actorSystem.getTraceInterface() != null) {
            actorSystem.getTraceInterface().onEnvelopeDelivered(envelope);
        }

        long start = ActorTime.currentTime();
        boolean isDisconnected = false;

        if (scope.getActor() == null) {
            if (envelope.getMessage() == PoisonPill.INSTANCE) {
                // Not creating actor for PoisonPill
                return;
            }
            try {
                Actor actor = scope.getProps().create();
                actor.initActor(scope.getPath(), new ActorContext(scope), scope.getMailbox());
                actor.preStart();
                scope.onActorCreated(actor);
            } catch (Exception e) {
                e.printStackTrace();
                if (envelope.getSender() != null) {
                    envelope.getSender().send(new DeadLetter("Unable to create actor"));
                }
                return;
            }
        }

        try {
            if (envelope.getMessage() == StartActor.INSTANCE) {
                // Already created actor
                return;
            } else if (envelope.getMessage() == PoisonPill.INSTANCE) {
                isDisconnected = true;
                scope.getActor().postStop();
                scope.onActorDie();
                for (Envelope e : scope.getMailbox().allEnvelopes()) {
                    if (e.getSender() != null) {
                        e.getSender().send(new DeadLetter(e.getMessage()));
                    }
                }
                scope.getMailbox().clear();
                synchronized (LOCK) {
                    scopes.remove(scope.getPath());
                    endpoints.remove(scope.getPath());
                    dispatcher.getQueue().disconnectMailbox(scope.getMailbox());
                }
            } else {
                scope.setSender(envelope.getSender());
                if (envelope.getMessage() instanceof Runnable) {
                    ((Runnable) envelope.getMessage()).run();
                    return;
                }
                scope.getActor().onReceive(envelope.getMessage());
            }
        } catch (Exception e) {
            if (actorSystem.getTraceInterface() != null) {
                actorSystem.getTraceInterface().onActorDie(scope.getActorRef(), envelope, e);
            }
            scope.onActorDie();
            isDisconnected = true;
            synchronized (LOCK) {
                scopes.remove(scope.getPath());
                endpoints.remove(scope.getPath());
                dispatcher.getQueue().disconnectMailbox(scope.getMailbox());
            }
        } finally {
            if (actorSystem.getTraceInterface() != null) {
                actorSystem.getTraceInterface().onEnvelopeProcessed(envelope, ActorTime.currentTime() - start);
            }
            if (!isDisconnected) {
                dispatcher.getQueue().unlockMailbox(envelope.getMailbox());
            }
        }
    }
}
