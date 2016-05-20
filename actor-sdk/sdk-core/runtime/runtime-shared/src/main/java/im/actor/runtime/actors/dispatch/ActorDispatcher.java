/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.runtime.actors.dispatch;

import java.util.HashMap;

import im.actor.runtime.Runtime;
import im.actor.runtime.actors.Actor;
import im.actor.runtime.actors.ActorContext;
import im.actor.runtime.actors.ActorRef;
import im.actor.runtime.actors.ActorScope;
import im.actor.runtime.actors.ActorSystem;
import im.actor.runtime.actors.ActorTime;
import im.actor.runtime.actors.Props;
import im.actor.runtime.actors.ThreadPriority;
import im.actor.runtime.actors.dispatch.queue.QueueCollection;
import im.actor.runtime.actors.dispatch.queue.QueueDispatcher;
import im.actor.runtime.actors.messages.DeadLetter;
import im.actor.runtime.actors.messages.PoisonPill;
import im.actor.runtime.actors.messages.StartActor;
import im.actor.runtime.function.Consumer;
import im.actor.runtime.threading.ThreadDispatcher;

/**
 * Abstract Actor Dispatcher, used for dispatching messages for actors
 */
public class ActorDispatcher {

    private final Object LOCK = new Object();
    private final HashMap<String, ActorEndpoint> endpoints = new HashMap<>();
    private final HashMap<String, ActorScope> scopes = new HashMap<>();

    private final ActorSystem actorSystem;
    private final QueueCollection<Envelope> queueCollection = new QueueCollection<>();
    private final String name;
    private final QueueDispatcher<Envelope>[] dispatchers;

    public ActorDispatcher(String name, ThreadPriority priority, ActorSystem actorSystem, int dispatchersCount) {
        this.name = name;
        this.actorSystem = actorSystem;
        this.dispatchers = new QueueDispatcher[dispatchersCount];
        final Consumer<Envelope> handler = envelope -> processEnvelope(envelope);
        for (int i = 0; i < dispatchers.length; i++) {
            this.dispatchers[i] = new QueueDispatcher<>(name + "_" + i, priority, queueCollection, handler);
        }
    }

    public String getName() {
        return name;
    }

    public final ActorRef referenceActor(String path, Props props) {
        synchronized (LOCK) {
            if (scopes.containsKey(path)) {
                return scopes.get(path).getActorRef();
            }

            Mailbox mailbox = new Mailbox(queueCollection);

            ActorEndpoint endpoint = endpoints.get(path);
            if (endpoint == null) {
                endpoint = new ActorEndpoint(path);
                endpoints.put(path, endpoint);
            }

            ActorScope scope = new ActorScope(actorSystem, mailbox, this, path, props, endpoint);
            endpoint.connect(mailbox, scope);
            scopes.put(scope.getPath(), scope);

            // Sending init message
            if (!Runtime.isSingleThread() && !Runtime.isMainThread()) {
                scope.getActorRef().send(StartActor.INSTANCE);
            } else {
                Runtime.dispatch(() -> scope.getActorRef().send(StartActor.INSTANCE));
            }
            return scope.getActorRef();
        }
    }


    /**
     * Processing of envelope
     *
     * @param envelope envelope
     */
    private void processEnvelope(Envelope envelope) {

        ActorScope scope = envelope.getScope();

        if (actorSystem.getTraceInterface() != null) {
            actorSystem.getTraceInterface().onEnvelopeDelivered(envelope);
        }

        long start = ActorTime.currentTime();
        if (scope.getActor() == null) {

            if (envelope.getMessage() == PoisonPill.INSTANCE) {
                // Not creating actor for PoisonPill
                return;
            }

            try {
                Actor actor = scope.getProps().create();
                actor.initActor(scope.getPath(), new ActorContext(scope), scope.getMailbox());
                ThreadDispatcher.pushDispatcher(actor.getDispatcher());
                try {
                    actor.preStart();
                } finally {
                    ThreadDispatcher.popDispatcher();
                }

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
            } else if (envelope.getMessage() == PoisonPill.INSTANCE) {
                ThreadDispatcher.pushDispatcher(scope.getActor().getDispatcher());
                try {
                    scope.getActor().postStop();
                } finally {
                    ThreadDispatcher.popDispatcher();
                }
                onActorDie(scope);
            } else {
                scope.getActor().handleMessage(envelope.getMessage(), envelope.getSender());
            }
        } catch (Exception e) {
            if (actorSystem.getTraceInterface() != null) {
                actorSystem.getTraceInterface().onActorDie(scope.getActorRef(), envelope, e);
            }
            ThreadDispatcher.pushDispatcher(scope.getActor().getDispatcher());
            try {
                scope.getActor().postStop();
            } finally {
                ThreadDispatcher.popDispatcher();
            }
            onActorDie(scope);
        } finally {
            if (actorSystem.getTraceInterface() != null) {
                actorSystem.getTraceInterface().onEnvelopeProcessed(envelope, ActorTime.currentTime() - start);
            }
        }
    }

    private void onActorDie(ActorScope scope) {
        scope.onActorDie();
        if (scope.getProps().getSupervisor() != null) {
            scope.getProps().getSupervisor().onActorStopped(scope.getActorRef());
        }
        Envelope[] deadLetters;
        synchronized (LOCK) {
            scopes.remove(scope.getPath());
            endpoints.remove(scope.getPath());
            deadLetters = scope.getMailbox().dispose();
        }
        for (Envelope e : deadLetters) {
            if (e.getSender() != null) {
                e.getSender().send(new DeadLetter(e.getMessage()));
            }
        }
    }
}
