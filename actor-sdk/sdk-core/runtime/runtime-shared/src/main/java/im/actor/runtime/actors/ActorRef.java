/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.runtime.actors;

import im.actor.runtime.actors.mailbox.ActorDispatcher;
import im.actor.runtime.actors.mailbox.ActorEndpoint;

/**
 * Reference to Actor that allows to send messages to real Actor
 */
public class ActorRef {

    private ActorSystem system;
    private ActorDispatcher dispatcher;
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
     * @param system     actor system
     * @param dispatcher dispatcher of actor
     * @param path       path of actor
     */
    public ActorRef(ActorEndpoint endpoint, ActorSystem system, ActorDispatcher dispatcher, String path) {
        this.endpoint = endpoint;
        this.system = system;
        this.dispatcher = dispatcher;
        this.path = path;
    }

    /**
     * Send message with empty sender
     *
     * @param message message
     */
    public void send(Object message) {
        dispatcher.sendMessageNow(endpoint, message, null);
    }

    /**
     * Send message with specified sender
     *
     * @param message message
     * @param sender  sender
     */
    public void send(Object message, ActorRef sender) {
        dispatcher.sendMessageNow(endpoint, message, sender);
    }

    /**
     * Sending message before all other messages
     *
     * @param message message
     * @param sender  sender
     */
    public void sendFirst(Object message, ActorRef sender) {
        dispatcher.sendMessageFirst(endpoint, message, sender);
    }

    /**
     * Sending message before all other messages
     *
     * @param message message
     */
    public void sendFirst(Object message) {
        dispatcher.sendMessageFirst(endpoint, message, null);
    }
}
