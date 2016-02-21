package im.actor.runtime.webrtc.sdp.entities;

import java.util.ArrayList;

public class SDPSession {

    private final int version;
    private final ArrayList<SDPRawRecord> records;
    private final String originator;
    private final String name;

    public SDPSession(int version, String originator, String name) {
        this.version = version;
        this.originator = originator;
        this.name = name;
        this.records = new ArrayList<>();
    }

    public int getVersion() {
        return version;
    }

    public String getOriginator() {
        return originator;
    }

    public String getName() {
        return name;
    }

    public ArrayList<SDPRawRecord> getRecords() {
        return records;
    }

    public String toSDP() {
        String res = "v=" + version + "\r\n";
        res += "o=" + originator + "\r\n";
        res += "s=" + name + "\r\n";
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
