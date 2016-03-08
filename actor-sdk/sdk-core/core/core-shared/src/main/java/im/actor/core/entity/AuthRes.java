package im.actor.core.entity;

public class AuthRes {

    private byte[] data;

    public AuthRes(byte[] data) {
        this.data = data;
    }

    public byte[] getData() {
        return data;
    }
}
