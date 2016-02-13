package im.actor.runtime.webrtc.sdp;

import java.util.ArrayList;
import java.util.List;

public class SDPSession {

    private final int version;
    private final ArrayList<SDPRawRecord> records;

    public SDPSession(int version) {
        this.version = version;
        this.records = new ArrayList<>();
    }

    public int getVersion() {
        return version;
    }

    public ArrayList<SDPRawRecord> getRecords() {
        return records;
    }

    @Override
    public String toString() {
        String res = "v=" + version + "\r\n";
        for (SDPRawRecord r : records) {
            res = r.getType() + "=" + r.getType() + "\r\n";
        }
        return res;
    }
}
