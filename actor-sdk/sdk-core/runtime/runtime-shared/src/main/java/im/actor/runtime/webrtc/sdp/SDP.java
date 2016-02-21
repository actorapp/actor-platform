package im.actor.runtime.webrtc.sdp;

import java.util.ArrayList;
import java.util.HashMap;

import im.actor.runtime.webrtc.sdp.entities.SDPMedia;
import im.actor.runtime.webrtc.sdp.entities.SDPMediaMode;
import im.actor.runtime.webrtc.sdp.entities.SDPRawRecord;
import im.actor.runtime.webrtc.sdp.entities.SDPSession;
import im.actor.runtime.webrtc.sdp.entities.SDPCodec;

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
            String protocol = mediaDesc[2];
            ArrayList<Integer> codecIds = new ArrayList<>();
            for (int i = 3; i < mediaDesc.length; i++) {
                codecIds.add(Integer.parseInt(mediaDesc[i]));
            }
            SDPMediaMode mode = SDPMediaMode.SEND_RECEIVE;
            ArrayList<SDPRawRecord> records = new ArrayList<>();
            // ArrayList<SDPCodec> codecs = new ArrayList<>();
            HashMap<Integer, SDPCodec> codecs = new HashMap<>();
            HashMap<Integer, HashMap<String, String>> args = new HashMap<>();
            HashMap<Integer, ArrayList<String>> codecFeedbackMessages = new HashMap<>();
            while ((record = reader.readUntil('m')) != null) {
                if (record.getType() == 'a' && "sendrecv".equals(record.getValue())) {
                    mode = SDPMediaMode.SEND_RECEIVE;
                } else if (record.getType() == 'a' && "inactive".equals(record.getValue())) {
                    mode = SDPMediaMode.INACTIVE;
                } else if (record.getType() == 'a' && "recvonly".equals(record.getValue())) {
                    mode = SDPMediaMode.RECEIVE_ONLY;
                } else if (record.getType() == 'a' && "sendonly".equals(record.getValue())) {
                    mode = SDPMediaMode.SEND_ONLY;
                } else if (record.getType() == 'a' && record.getValue().startsWith("rtpmap:")) {
                    String[] codecMap = record.getValue().split(" ", 2);
                    int index = Integer.parseInt(codecMap[0].substring("rtpmap:".length()));
                    String[] codecDef = codecMap[1].split("/");
                    String codecName = codecDef[0];
                    int clockRate = Integer.parseInt(codecDef[1]);
                    String codecArgs = codecDef.length >= 3 ? codecDef[2] : null;
                    codecs.put(index, new SDPCodec(index, codecName, clockRate, codecArgs));
                } else if (record.getType() == 'a' && record.getValue().startsWith("fmtp:")) {
                    String[] codecMap = record.getValue().split(" ", 2);
                    int index = Integer.parseInt(codecMap[0].substring("fmtp:".length()));
                    String params = codecMap[1];
                    String[] pLines = params.trim().split(";");
                    HashMap<String, String> p = new HashMap<>();
                    for (String s : pLines) {
                        s = s.trim();
                        String[] v2 = s.split("=", 2);
                        p.put(v2[0], v2[1]);
                    }
                    args.put(index, p);
                } else if (record.getType() == 'a' && record.getValue().startsWith("rtcp-fb:")) {
                    String[] codecMap = record.getValue().split(" ", 2);
                    int index = Integer.parseInt(codecMap[0].substring("rtcp-fb:".length()));
                    if (!codecFeedbackMessages.containsKey(index)) {
                        codecFeedbackMessages.put(index, new ArrayList<String>());
                    }
                    codecFeedbackMessages.get(index).add(codecMap[1]);
                } else {
                    records.add(record);
                }
            }

            ArrayList<SDPCodec> codec = new ArrayList<>();
            for (int index : codecIds) {
                SDPCodec sdpCodec = codecs.get(index);
                sdpCodec.setFormat(args.get(index));
                sdpCodec.setCodecFeedback(codecFeedbackMessages.get(index));
                codec.add(sdpCodec);
            }
            medias.add(new SDPMedia(mediaType, port, protocol, codec, mode, records));
        }

        return new SDPScheme(session, medias);
    }
}