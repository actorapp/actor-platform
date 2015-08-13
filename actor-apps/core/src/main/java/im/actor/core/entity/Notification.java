/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.core.entity;

public class Notification {
    private PeerEntity peer;
    private int sender;
    private ContentDescription contentDescription;

    public Notification(PeerEntity peer, int sender, ContentDescription contentDescription) {
        this.peer = peer;
        this.sender = sender;
        this.contentDescription = contentDescription;
    }

    public PeerEntity getPeer() {
        return peer;
    }

    public int getSender() {
        return sender;
    }

    public ContentDescription getContentDescription() {
        return contentDescription;
    }
}