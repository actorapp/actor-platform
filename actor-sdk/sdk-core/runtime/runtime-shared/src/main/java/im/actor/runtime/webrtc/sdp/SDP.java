package im.actor.runtime.webrtc.sdp;

import java.util.ArrayList;

import im.actor.runtime.webrtc.sdp.entities.SDPMedia;
import im.actor.runtime.webrtc.sdp.entities.SDPMediaMode;
import im.actor.runtime.webrtc.sdp.entities.SDPRawRecord;
import im.actor.runtime.webrtc.sdp.entities.SDPSession;

public abstract class SDP {

    public static SDPScheme parse(String sdp) {
        SDPReader reader = new SDPReader(sdp);
        SDPRawRecord record;

        //
        // Session
        //
        int version = reader.readVersion();
        String originator = reader.readRecord('o').getValue();
        String sessionName = reader.readRecord('s').getValue();
        SDPSession session = new SDPSession(version, originator, sessionName);
        while ((record = reader.readUntil('m')) != null) {
            session.getRecords().add(record);
        }

        //
        // Media Lines
        //
        ArrayList<SDPMedia> medias = new ArrayList<>();
        SDPRawRecord mediaLine;
        while ((mediaLine = reader.readOptionalRecord('m')) != null) {
            String[] mediaDesc = mediaLine.getValue().split(" ");
            String mediaType = mediaDesc[0];
            int port = Integer.parseInt(mediaDesc[1]);
            ArrayList<String> protocols = new ArrayList<>();
            for (String p : mediaDesc[2].split("/")) {
                protocols.add(p);
            }
            ArrayList<Integer> codecs = new ArrayList<>();
            for (int i = 3; i < mediaDesc.length; i++) {
                codecs.add(Integer.parseInt(mediaDesc[i]));
            }
            SDPMediaMode mode = SDPMediaMode.SEND_RECEIVE;
            ArrayList<SDPRawRecord> records = new ArrayList<>();
            while ((record = reader.readUntil('m')) != null) {
                if (record.getType() == 'a' && "sendrecv".equals(record.getValue())) {
                    mode = SDPMediaMode.SEND_RECEIVE;
                } else if (record.getType() == 'a' && "inactive".equals(record.getValue())) {
                    mode = SDPMediaMode.INACTIVE;
                } else if (record.getType() == 'a' && "recvonly".equals(record.getValue())) {
                    mode = SDPMediaMode.RECEIVE_ONLY;
                } else if (record.getType() == 'a' && "sendonly".equals(record.getValue())) {
                    mode = SDPMediaMode.SEND_ONLY;
                } else {
                    records.add(record);
                }
            }
            medias.add(new SDPMedia(mediaType, port, protocols, codecs, mode, records));
        }

        return new SDPScheme(session, medias);
    }
}