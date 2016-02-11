package im.actor.runtime.webrtc.sdp;

import java.util.ArrayList;

public class SDPMedia {
    private ArrayList<SDPRawRecord> records;

    public SDPMedia(ArrayList<SDPRawRecord> records) {
        this.records = records;
    }

    public ArrayList<SDPRawRecord> getRecords() {
        return records;
    }
}
