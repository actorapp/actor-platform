/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.runtime.actors;

import im.actor.runtime.actors.dispatch.ActorDispatcher;
import im.actor.runtime.actors.dispatch.ActorEndpoint;
import im.actor.runtime.actors.dispatch.Envelope;

/**
 * Reference to Actor that allows to send messages to real Actor
 */
public class ActorRef {

    private ActorSystem system;
    private String path;
    private ActorEndpoint endpoint;

    public String getPath() {
        return path;
    }

    public ActorSystem system() {
        return system;
    }

    /**
     * <p>INTERNAL API</p>
     * Creating actor reference
     *
     * @param system actor system
     * @param path   path of actor
     */
    public ActorRef(ActorEndpoint endpoint, ActorSystem system, String path) {
        this.endpoint = endpoint;
        this.system = system;
        this.path = path;
    }

    /**
     * Send message with empty sender
     *
     * @param message message
     */
    public void send(Object message) {
        send(message, null);
    }

    /**
     * Execute on Actor Thread
     *
     * @param runnable runnable
     */
    public void post(Runnable runnable) {
        send(runnable);
    }

    /**
     * Send message with specified sender
     *
     * @param message message
     * @param sender  sender
     */
    public void send(Object message, ActorRef sender) {
        endpoint.getMailbox().schedule(new Envelope(message, endpoint.getScope(), endpoint.getMailbox(), sender));
    }

    /**
     * Sending message before all other messages
     *
     * @param message message
     * @param sender  sender
     */
    public void sendFirst(Object message, ActorRef sender) {
        endpoint.getMailbox().scheduleFirst(new Envelope(message, endpoint.getScope(), endpoint.getMailbox(), sender));
    }
}
