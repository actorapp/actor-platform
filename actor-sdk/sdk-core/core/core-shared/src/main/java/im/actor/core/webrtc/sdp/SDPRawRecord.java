package im.actor.core.webrtc.sdp;

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
}
