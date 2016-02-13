package im.actor.runtime.webrtc.sdp;

import java.util.ArrayList;

public class SDPMedia {

    private String type;
    private ArrayList<String> protocols;
    private ArrayList<Integer> codecs;
    private ArrayList<String> args;
    private ArrayList<SDPRawRecord> records;

    public SDPMedia(String type, ArrayList<String> protocols, ArrayList<Integer> codecs,
                    ArrayList<String> args, ArrayList<SDPRawRecord> records) {
        this.type = type;
        this.protocols = protocols;
        this.codecs = codecs;
        this.records = records;
        this.args = args;
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

    public ArrayList<String> getArgs() {
        return args;
    }

    @Override
    public String toString() {
        String res = "m=" + type + " " + codecs.size();
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
        for (String a : args) {
            res += " " + a;
        }
        res += "\r\n";

        for (SDPRawRecord r : records) {
            res += r.toString() + "\r\n";
        }
        return res;
    }
}
