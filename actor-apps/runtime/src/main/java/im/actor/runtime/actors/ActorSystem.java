/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.runtime.actors;

import java.util.HashMap;

import im.actor.runtime.Runtime;
import im.actor.runtime.actors.mailbox.ActorDispatcher;

/**
 * Entry point for Actor Model, creates all actors and dispatchers
 */
public class ActorSystem {

    private static final ActorSystem mainSystem = new ActorSystem();

    /**
     * Main actor system
     *
     * @return ActorSystem
     */
    public static ActorSystem system() {
        return mainSystem;
    }

    private static final String DEFAULT_DISPATCHER = "default";

    private final HashMap<String, ActorDispatcher> dispatchers = new HashMap<String, ActorDispatcher>();

    private TraceInterface traceInterface;

    /**
     * Creating new actor system
     */
    public ActorSystem() {
        this(true);
    }


    /**
     * Creating new actor system
     */
    public ActorSystem(boolean addDefaultDispatcher) {
        if (addDefaultDispatcher) {
            addDispatcher(DEFAULT_DISPATCHER);
        }
    }

    /**
     * Adding dispatcher with specific threads count
     *
     * @param dispatcherId dispatcher id
     * @param threadsCount threads count
     */
    public void addDispatcher(String dispatcherId, int threadsCount) {
        synchronized (dispatchers) {
            if (dispatchers.containsKey(dispatcherId)) {
                return;
            }

            ActorDispatcher dispatcher = im.actor.runtime.Runtime.createDispatcher(dispatcherId, threadsCount, ThreadPriority.LOW, this);
            dispatchers.put(dispatcherId, dispatcher);
        }
    }

    /**
     * Adding dispatcher with threads count = {@code Runtime.getRuntime().availableProcessors()}
     *
     * @param dispatcherId dispatcher id
     */
    public void addDispatcher(String dispatcherId) {
        synchronized (dispatchers) {
            if (dispatchers.containsKey(dispatcherId)) {
                return;
            }

            ActorDispatcher dispatcher = Runtime.createDefaultDispatcher(dispatcherId, ThreadPriority.LOW, this);
            addDispatcher(dispatcherId, dispatcher);
        }
    }

    /**
     * Registering custom dispatcher
     *
     * @param dispatcherId dispatcher id
     * @param dispatcher   dispatcher object
     */
    public void addDispatcher(String dispatcherId, ActorDispatcher dispatcher) {
        synchronized (dispatchers) {
            if (dispatchers.containsKey(dispatcherId)) {
                return;
            }
            dispatchers.put(dispatcherId, dispatcher);
        }
    }

    public <T extends Actor> ActorRef actorOf(ActorSelection selection) {
        return actorOf(selection.getProps(), selection.getPath());
    }

    /**
     * Creating or getting existing actor from actor props
     *
     * @param props Actor Props
     * @param path  Actor Path
     * @return ActorRef
     */
    public ActorRef actorOf(Props props, String path) {
        String dispatcherId = props.getDispatcher() == null ? DEFAULT_DISPATCHER : props.getDispatcher();

        ActorDispatcher mailboxesDispatcher;
        synchronized (dispatchers) {
            if (!dispatchers.containsKey(dispatcherId)) {
                throw new RuntimeException("Unknown dispatcherId '" + dispatcherId + "'");
            }
            mailboxesDispatcher = dispatchers.get(dispatcherId);
        }

        return mailboxesDispatcher.referenceActor(path, props);
    }

    /**
     * Getting current trace interface for actor system
     *
     * @return trace interface
     */
    public TraceInterface getTraceInterface() {
        return traceInterface;
    }

    /**
     * Setting current trace interface for actor system
     *
     * @param traceInterface trace interface
     */
    public void setTraceInterface(TraceInterface traceInterface) {
        this.traceInterface = traceInterface;
    }
}