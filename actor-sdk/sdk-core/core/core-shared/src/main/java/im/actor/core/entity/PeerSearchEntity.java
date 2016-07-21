package im.actor.core.entity;

public class PeerSearchEntity {

    private Peer peer;
    private String optMatchString;

    public PeerSearchEntity(Peer peer, String optMatchString) {
        this.peer = peer;
        this.optMatchString = optMatchString;
    }

    public Peer getPeer() {
        return peer;
    }

    public String getOptMatchString() {
        return optMatchString;
    }
}
