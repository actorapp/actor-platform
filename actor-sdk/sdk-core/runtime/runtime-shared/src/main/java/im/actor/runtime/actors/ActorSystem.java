/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.runtime.actors;

import java.util.HashMap;

import im.actor.runtime.Runtime;
import im.actor.runtime.actors.dispatch.ActorDispatcher;
import im.actor.runtime.function.Constructor;

/**
 * Entry point for Actor Model, creates all actors and dispatchers
 */
public class ActorSystem {

    public static final float THREAD_MULTIPLIER = 1.5f;
    public static final int THREAD_MAX_COUNT = 4;

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

            ActorDispatcher dispatcher = new ActorDispatcher(dispatcherId, ThreadPriority.LOW, this,
                    Runtime.isSingleThread() ? 1 : threadsCount);

            dispatchers.put(dispatcherId, dispatcher);
        }
    }

    /**
     * Adding dispatcher with threads count = {@code Runtime.getRuntime().availableProcessors()}
     *
     * @param dispatcherId dispatcher id
     */
    public void addDispatcher(String dispatcherId) {
        addDispatcher(dispatcherId,
                Math.min((int) (Runtime.getCoresCount() * THREAD_MULTIPLIER), THREAD_MAX_COUNT));
    }


    public ActorRef actorOf(ActorSelection selection) {
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

    public ActorRef actorOf(String path, Props props) {
        return actorOf(props, path);
    }

    public ActorRef actorOf(String path, ActorCreator creator) {
        return actorOf(Props.create(creator), path);
    }

    public ActorRef actorOf(String path, ActorCreator creator, ActorSupervisor supervisor) {
        return actorOf(Props.create(creator)
                .changeSupervisor(supervisor), path);
    }

//    public ActorRef actorOf(String path, final Constructor<? extends Actor> constructor) {
//        return actorOf(Props.create(new ActorCreator() {
//            @Override
//            public Actor create() {
//                return constructor.create();
//            }
//        }), path);
//    }

    public ActorRef actorOf(String path, String dispatcher, ActorCreator creator) {
        return actorOf(Props.create(creator).changeDispatcher(dispatcher), path);
    }

    public ActorRef actorOf(String path, String dispatcher, ActorCreator creator, ActorSupervisor supervisor) {
        return actorOf(Props.create(creator)
                .changeDispatcher(dispatcher)
                .changeSupervisor(supervisor), path);
    }

    public ActorRef actorOf(String path, String dispatcher, final Constructor<? extends Actor> constructor) {
        return actorOf(Props.create(new ActorCreator() {
            @Override
            public Actor create() {
                return constructor.create();
            }
        }).changeDispatcher(dispatcher), path);
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