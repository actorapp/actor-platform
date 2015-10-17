/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.runtime.actors.mailbox;

import com.google.j2objc.annotations.Property;

import im.actor.runtime.actors.ActorRef;
import im.actor.runtime.actors.ActorScope;
import im.actor.runtime.actors.ActorTime;

/**
 * Actor system envelope
 */
public class Envelope {

    @Property("readonly, nonatomic")
    private final Object message;
    @Property("readonly, nonatomic")
    private final ActorRef sender;
    @Property("readonly, nonatomic")
    private final Mailbox mailbox;

    @Property
    private final ActorScope scope;
    @Property
    private final long sendTime;

    /**
     * Creating of envelope
     *
     * @param message message
     * @param mailbox mailbox
     * @param sender  sender reference
     */
    public Envelope(Object message, ActorScope scope, Mailbox mailbox, ActorRef sender) {
        this.scope = scope;
        this.message = message;
        this.sender = sender;
        this.mailbox = mailbox;
        this.sendTime = ActorTime.currentTime();
    }

    public ActorScope getScope() {
        return scope;
    }

    /**
     * Message in envelope
     *
     * @return message
     */
    public Object getMessage() {
        return message;
    }

    /**
     * Mailbox for envelope
     *
     * @return mailbox
     */
    public Mailbox getMailbox() {
        return mailbox;
    }

    /**
     * Sender of message
     *
     * @return sender reference
     */
    public ActorRef getSender() {
        return sender;
    }

    public long getSendTime() {
        return sendTime;
    }

    @Override
    public String toString() {
        return "{" + message + " -> " + scope.getPath() + "}";
    }
}
