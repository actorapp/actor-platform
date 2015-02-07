package com.droidkit.actors.mailbox;

import com.droidkit.actors.ActorRef;
import com.droidkit.actors.ActorScope;
import com.droidkit.actors.ActorTime;

/**
 * Actor system envelope
 *
 * @author Steve Ex3NDR Korshakov (steve@actor.im)
 */
public class Envelope {
    private final Object message;
    private final ActorRef sender;
    private final Mailbox mailbox;
    private ActorScope scope;
    private long sendTime;

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
