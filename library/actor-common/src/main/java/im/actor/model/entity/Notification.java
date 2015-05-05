/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.model.entity;

public class Notification {
    private Peer peer;
    private int sender;
    private ContentDescription contentDescription;

    public Notification(Peer peer, int sender, ContentDescription contentDescription) {
        this.peer = peer;
        this.sender = sender;
        this.contentDescription = contentDescription;
    }

    public Peer getPeer() {
        return peer;
    }

    public int getSender() {
        return sender;
    }

    public ContentDescription getContentDescription() {
        return contentDescription;
    }
}