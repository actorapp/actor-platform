package im.actor.model.entity;

/**
 * Created by ex3ndr on 01.03.15.
 */
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