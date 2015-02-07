package com.droidkit.actors;

import com.droidkit.actors.conf.DispatcherFactory;
import com.droidkit.actors.conf.EnvConfig;
import com.droidkit.actors.debug.TraceInterface;
import com.droidkit.actors.mailbox.ActorDispatcher;

import java.util.HashMap;

/**
 * Entry point for Actor Model, creates all actors and dispatchers
 *
 * @author Steve Ex3NDR Korshakov (steve@actor.im)
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

    private ClassLoader classLoader;

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
        classLoader = getClass().getClassLoader();
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

            ActorDispatcher dispatcher = EnvConfig.createDispatcher(dispatcherId, threadsCount, ThreadPriority.LOW, this);
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

            ActorDispatcher dispatcher = EnvConfig.createDispatcher(dispatcherId, Runtime.getRuntime().availableProcessors(), ThreadPriority.LOW, this);
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
     * Creating or getting existing actor from actor class
     *
     * @param actor Actor Class
     * @param path  Actor Path
     * @param <T>   Actor Class
     * @return ActorRef
     */
    public <T extends Actor> ActorRef actorOf(Class<T> actor, String path) {
        return actorOf(Props.create(actor), path);
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

    /**
     * Getting actor system class loader
     *
     * @return class loader
     */
    public ClassLoader getClassLoader() {
        return classLoader;
    }

    /**
     * Setting actor system class loader
     *
     * @param classLoader class loader
     */
    public void setClassLoader(ClassLoader classLoader) {
        if (classLoader == null) {
            throw new IllegalArgumentException("ClassLoader cannot be null");
        }
        this.classLoader = classLoader;
    }
}