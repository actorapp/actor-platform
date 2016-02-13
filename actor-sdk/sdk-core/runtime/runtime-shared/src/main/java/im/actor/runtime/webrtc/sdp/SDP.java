package im.actor.runtime.webrtc.sdp;

import java.util.ArrayList;

public abstract class SDP {

    public static SDPScheme parse(String sdp) {
        SDPReader reader = new SDPReader(sdp);
        SDPRawRecord record;

        //
        // Session
        //
        SDPSession session = new SDPSession(reader.readVersion());
        while ((record = reader.readUntil('m')) != null) {
            session.getRecords().add(record);
        }

        //
        // Media Lines
        //
        ArrayList<SDPMedia> medias = new ArrayList<>();
        SDPRawRecord mediaLine;
        while ((mediaLine = reader.readRecord('m')) != null) {
            String[] mediaDesc = mediaLine.getValue().split(" ");
            String mediaType = mediaDesc[0];
            int codecsCount = Integer.parseInt(mediaDesc[1]);
            ArrayList<String> protocols = new ArrayList<>();
            for (String p : mediaDesc[2].split("/")) {
                protocols.add(p);
            }
            ArrayList<Integer> codecs = new ArrayList<>();
            for (int i = 0; i < codecsCount; i++) {
                codecs.add(Integer.parseInt(mediaDesc[3 + i]));
            }
            ArrayList<String> args = new ArrayList<>();
            for (int i = codecsCount + 3; i < mediaDesc.length; i++) {
                args.add(mediaDesc[i]);
            }
            ArrayList<SDPRawRecord> records = new ArrayList<>();
            while ((record = reader.readUntil('m')) != null) {
                records.add(record);
            }
            medias.add(new SDPMedia(mediaType, protocols, codecs, args, records));
        }

        return new SDPScheme(session, medias);
    }
}