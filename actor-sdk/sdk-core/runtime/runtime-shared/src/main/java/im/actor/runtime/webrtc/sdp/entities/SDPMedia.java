package im.actor.runtime.webrtc.sdp.entities;

import java.util.ArrayList;

public class SDPMedia {

    private String type;
    private int port;
    private String protocol;
    private ArrayList<SDPCodec> codecs;
    private ArrayList<SDPRawRecord> records;
    private SDPMediaMode mode;

    public SDPMedia(String type, int port, String protocol, ArrayList<SDPCodec> codecs,
                    SDPMediaMode mode, ArrayList<SDPRawRecord> records) {
        this.type = type;
        this.port = port;
        this.protocol = protocol;
        this.codecs = codecs;
        this.records = records;
        this.mode = mode;

    }

    public String getType() {
        return type;
    }

    public int getPort() {
        return port;
    }

    public String getProtocol() {
        return protocol;
    }

    public ArrayList<SDPCodec> getCodecs() {
        return codecs;
    }

    public ArrayList<SDPRawRecord> getRecords() {
        return records;
    }

    public SDPMediaMode getMode() {
        return mode;
    }

    public String toSDP() {
        String res = "m=" + type + " " + port + " " + protocol;
        for (SDPCodec codec : codecs) {
            res += " " + codec.getIndex();
        }
        res += "\r\n";
        switch (mode) {
            case SEND_RECEIVE:
                res += "a=sendrecv\r\n";
                break;
            case INACTIVE:
                res += "a=inactive\r\n";
                break;
            case RECEIVE_ONLY:
                res += "a=recvonly\r\n";
                break;
            case SEND_ONLY:
                res += "a=sendonly\r\n";
                break;
        }
        for (SDPCodec codec : codecs) {
            res += codec.toSDP();
        }
        for (SDPRawRecord r : records) {
            res += r.toSDP() + "\r\n";
        }
        return res;
    }

    @Override
    public String toString() {
        return toSDP();
    }
}
