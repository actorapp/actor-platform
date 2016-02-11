package im.actor.runtime.webrtc.sdp;

import java.util.ArrayList;
import java.util.List;

public class SDPSession {

    private final int version;
    private final ArrayList<SDPRawRecord> records;

    public SDPSession(int version, List<SDPRawRecord> records) {
        this.version = version;
        this.records = new ArrayList<>(records);
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
