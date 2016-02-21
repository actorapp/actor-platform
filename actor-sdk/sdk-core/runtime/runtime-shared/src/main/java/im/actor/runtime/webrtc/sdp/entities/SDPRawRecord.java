package im.actor.runtime.webrtc.sdp.entities;

public class SDPRawRecord {

    private char type;
    private String value;

    public SDPRawRecord(char type, String value) {
        this.type = type;
        this.value = value;
    }

    public char getType() {
        return type;
    }

    public String getValue() {
        return value;
    }

    public String toSDP() {
        return type + "=" + value;
    }

    @Override
    public String toString() {
        return toSDP();
    }
}
