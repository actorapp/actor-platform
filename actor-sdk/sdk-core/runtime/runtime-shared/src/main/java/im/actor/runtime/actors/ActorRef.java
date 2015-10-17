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
     * Send message with empty sender and delay
     *
     * @param message message
     * @param delay   delay
     */
    public void send(Object message, long delay) {
        dispatcher.sendMessageAtTime(endpoint, message, ActorTime.currentTime() + delay, null);
    }

    /**
     * Send message
     *
     * @param message message
     * @param delay   delay
     * @param sender  sender
     */
    public void send(Object message, long delay, ActorRef sender) {
        dispatcher.sendMessageAtTime(endpoint, message, ActorTime.currentTime() + delay, sender);
    }

    /**
     * Send message once
     *
     * @param message message
     */
    public void sendOnce(Object message) {
        dispatcher.sendMessageOnceNow(endpoint, message, null);
    }

    /**
     * Send message once
     *
     * @param message message
     * @param sender  sender
     */
    public void sendOnce(Object message, ActorRef sender) {
        dispatcher.sendMessageOnceNow(endpoint, message, sender);
    }

    /**
     * Send message once
     *
     * @param message message
     * @param delay   delay
     */
    public void sendOnce(Object message, long delay) {
        dispatcher.sendMessageOnceAtTime(endpoint, message, ActorTime.currentTime() + delay, null);
    }

    /**
     * Send message once
     *
     * @param message message
     * @param delay   delay
     * @param sender  sender
     */
    public void sendOnce(Object message, long delay, ActorRef sender) {
        dispatcher.sendMessageOnceAtTime(endpoint, message, ActorTime.currentTime() + delay, sender);
    }

    /**
     * Cancelling scheduled message
     *
     * @param message message
     */
    public void cancelMessage(Object message) {
        cancelMessage(message, null);
    }

    /**
     * Cancelling scheduled message
     *
     * @param message message
     * @param sender  sender
     */
    public void cancelMessage(Object message, ActorRef sender) {
        dispatcher.cancelSend(endpoint, message, sender);
    }
}
