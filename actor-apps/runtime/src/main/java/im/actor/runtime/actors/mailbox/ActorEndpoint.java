/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.runtime.actors.mailbox;

import com.google.j2objc.annotations.Property;

import im.actor.runtime.actors.ActorScope;

public class ActorEndpoint {
    @Property("readonly, nonatomic")
    private final String path;
    @Property
    private Mailbox mailbox;
    @Property
    private ActorScope scope;
    @Property
    private boolean isDisconnected;

    public ActorEndpoint(String path) {
        this.path = path;
        isDisconnected = false;
    }

    public String getPath() {
        return path;
    }

    public Mailbox getMailbox() {
        return mailbox;
    }

    public ActorScope getScope() {
        return scope;
    }

    public boolean isDisconnected() {
        return isDisconnected;
    }

    public void connect(Mailbox mailbox, ActorScope scope) {
        isDisconnected = false;
        this.mailbox = mailbox;
        this.scope = scope;
    }
}
