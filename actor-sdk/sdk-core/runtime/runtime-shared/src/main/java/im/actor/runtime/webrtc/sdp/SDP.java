package im.actor.runtime.webrtc.sdp;

public abstract class SDP {
    public static SDPScheme parse(String sdp) {

        SDPReader reader = new SDPReader(sdp);

//        String[] lines = sdp.split("\\r?\\n");
//        int cursor = 0;
//
//        //
//        // Header
//        //
//        if (!lines[cursor].startsWith("v=")) {
//            throw new RuntimeException("First line doesn't start with version of SDP");
//        }
//        int ver = Integer.parseInt(lines[cursor].substring(2));
//        cursor++;

//        //
//        // Session
//        //
//        SDPSession session = new SDPSession(ver);
//        while (cursor < lines.length) {
//            // Reached media section
//            if (lines[cursor].startsWith("m=")) {
//                break;
//            }
//            char type = lines[cursor].charAt(0);
//            String val = lines[cursor].substring(2);
//            session.getRecords().add(new SDPRawRecord(type, val));
//            cursor++;
//        }
//
//        //
//        // Media Lines
//        //
//        while (cursor < lines.length) {
//            String mediaStartLine = lines[cursor++];
//            while (cursor < lines.length) {
//                if (lines[cursor].startsWith("m=")) {
//                    break;
//                }
//                cursor++;
//            }
//        }

        return null;
    }
}