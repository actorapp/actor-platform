package im.actor.runtime.webrtc.sdp;

class SDPReader {

    private SDPRawRecord[] records;
    private int cursor;

    public SDPReader(String sdp) {
        String[] lines = sdp.split("\\r?\\n");
        records = new SDPRawRecord[lines.length];
        for (int i = 0; i < lines.length; i++) {
            String line = lines[i];
            if (line.charAt(1) != '=') {
                throw new RuntimeException("Incorrect Line #" + i + " in SDP");
            }
            records[i] = new SDPRawRecord(line.charAt(0), line.substring(2));
        }
        cursor = 0;
    }

    public SDPRawRecord readUntil(char v) {
        if (cursor < records.length) {
            if (records[cursor].getType() != v) {
                return records[cursor++];
            } else {
                return null;
            }
        }
        return null;
    }

    public SDPRawRecord readRecord() {
        if (cursor < records.length) {
            return records[cursor++];
        }
        return null;
    }

    public SDPRawRecord readRecord(char v) {
        if (cursor < records.length) {
            if (records[cursor].getType() == v) {
                return records[cursor++];
            } else {
                throw new RuntimeException("Type mismatch. Expected: " + v + ", got: " + records[cursor].getType());
            }
        }
        return null;
    }

    public int readVersion() {
        return Integer.parseInt(readRecord('v').getValue());
    }
}