/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.core.entity;

import com.google.j2objc.annotations.Property;

public class Notification {
    @Property("readonly, nonatomic")
    private Peer peer;
    @Property("readonly, nonatomic")
    private boolean isChannel;
    @Property("readonly, nonatomic")
    private int sender;
    @Property("readonly, nonatomic")
    private ContentDescription contentDescription;

    public Notification(Peer peer, boolean isChannel, int sender, ContentDescription contentDescription) {
        this.peer = peer;
        this.isChannel = isChannel;
        this.sender = sender;
        this.contentDescription = contentDescription;
    }

    public Peer getPeer() {
        return peer;
    }

    public int getSender() {
        return sender;
    }

    public boolean isChannel() {
        return isChannel;
    }

    public ContentDescription getContentDescription() {
        return contentDescription;
    }
}