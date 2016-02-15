package im.actor.runtime.webrtc.sdp.entities;

import java.util.ArrayList;

public class SDPMedia {

    private String type;
    private int port;
    private ArrayList<String> protocols;
    private ArrayList<Integer> codecs;
    private ArrayList<SDPRawRecord> records;
    private SDPMediaMode mode;

    public SDPMedia(String type, int port, ArrayList<String> protocols, ArrayList<Integer> codecs,
                    SDPMediaMode mode, ArrayList<SDPRawRecord> records) {
        this.type = type;
        this.port = port;
        this.protocols = protocols;
        this.codecs = codecs;
        this.records = records;
        this.mode = mode;

    }

    public String getType() {
        return type;
    }

    public ArrayList<String> getProtocols() {
        return protocols;
    }

    public ArrayList<Integer> getCodecs() {
        return codecs;
    }

    public ArrayList<SDPRawRecord> getRecords() {
        return records;
    }

    public SDPMediaMode getMode() {
        return mode;
    }

    @Override
    public String toString() {
        String res = "m=" + type + " " + port;
        String proto = "";
        for (String p : protocols) {
            if (proto.length() > 0) {
                proto += "/";
            }
            proto += p;
        }
        res += " " + proto;
        for (Integer codec : codecs) {
            res += " " + codec;
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
        for (SDPRawRecord r : records) {
            res += r.toString() + "\r\n";
        }
        return res;
    }
}
