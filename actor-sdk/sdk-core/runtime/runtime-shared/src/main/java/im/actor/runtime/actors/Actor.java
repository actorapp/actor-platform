/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.runtime.actors;

import java.util.ArrayList;

import im.actor.runtime.actors.mailbox.Mailbox;
import im.actor.runtime.actors.messages.DeadLetter;
import im.actor.runtime.actors.messages.StashBegin;
import im.actor.runtime.actors.messages.StashEnd;
import im.actor.runtime.actors.messages.StashIgnore;

/**
 * Actor object
 */
public class Actor {

    private String path;

    private ActorContext context;
    private Mailbox mailbox;

    private ArrayList<StashedMessage> stashed = new ArrayList<StashedMessage>();
    private Runnable beforeUnstash = null;
    private boolean isStashing = false;

    public Actor() {

    }

    /**
     * <p>INTERNAL API</p>
     * Initialization of actor
     *
     * @param path    path of actor
     * @param context context of actor
     * @param mailbox mailbox of actor
     */
    public final void initActor(String path, ActorContext context, Mailbox mailbox) {
        this.path = path;
        this.context = context;
        this.mailbox = mailbox;
    }

    /**
     * <p>INTERNAL API</p>
     * Handling of a message in Actor
     *
     * @param message message
     */
    public final void handleMessage(Object message, ActorRef sender) {
        if (message instanceof StashEnd) {
            endStash();
        } else if (message instanceof StashBegin) {
            beginStash(null);
        } else if (message instanceof StashIgnore) {
            intHandle(((StashIgnore) message).getMessage(), sender);
        } else {
            // Stashing message
            if (isStashing) {
                stashed.add(new StashedMessage(message, sender));
                return;
            }
            intHandle(message, sender);
        }
    }

    public void beginStash(Runnable beforeUnstash) {
        if (isStashing) {
            throw new RuntimeException("Actor is already stashed");
        }
        this.beforeUnstash = beforeUnstash;
        isStashing = true;
    }

    public void endStash() {
        if (!isStashing) {
            throw new RuntimeException("Actor is not stashed");
        }
        isStashing = false;

        if (beforeUnstash != null) {
            beforeUnstash.run();
            beforeUnstash = null;
        }

        StashedMessage[] msgs = stashed.toArray(new StashedMessage[stashed.size()]);
        stashed.clear();
        for (int i = msgs.length - 1; i >= 0; i--) {
            self().sendFirst(msgs[i].getMessage(), msgs[i].getSender());
        }
    }

    private void intHandle(Object message, ActorRef sender) {
        context.setSender(sender);

        if (message instanceof Runnable) {
            ((Runnable) message).run();
            return;
        }
        onReceive(message);
    }

    /**
     * Actor System
     *
     * @return Actor System
     */
    public final ActorSystem system() {
        return context.getSystem();
    }

    /**
     * Self actor reference
     *
     * @return self reference
     */
    public final ActorRef self() {
        return context.getSelf();
    }

//    /**
//     * Actor context
//     *
//     * @return context
//     */
//    protected final ActorContext context() {
//        return context;
//    }

    /**
     * Sender of last received message
     *
     * @return sender's ActorRef
     */
    public final ActorRef sender() {
        return context.sender();
    }

    /**
     * Actor path
     *
     * @return path
     */
    protected final String getPath() {
        return path;
    }

    /**
     * Actor mailbox
     *
     * @return mailbox
     */
    public final Mailbox getMailbox() {
        return mailbox;
    }

    /**
     * Called before first message receiving
     */
    public void preStart() {

    }

    /**
     * Receiving of message
     *
     * @param message message
     */
    public void onReceive(Object message) {
        drop(message);
    }

    /**
     * Called after actor shutdown
     */
    public void postStop() {

    }

    /**
     * finally-like method before actor death
     */
    public void finallyStop() {

    }

    /**
     * Reply message to sender of last message
     *
     * @param message reply message
     */
    public void reply(Object message) {
        if (context.sender() != null) {
            context.sender().send(message, self());
        }
    }

    /**
     * Dropping of message
     *
     * @param message message for dropping
     */
    public void drop(Object message) {
        if (system().getTraceInterface() != null) {
            system().getTraceInterface().onDrop(sender(), message, this);
        }
        reply(new DeadLetter(message));
    }
}