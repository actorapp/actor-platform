package im.actor.core.modules.calls.peers;

import im.actor.core.modules.calls.entity.PeerNodeSettings;

public class SDPOptimizer {

    private final PeerNodeSettings ownSettings;
    private final PeerNodeSettings theirSettings;

    public SDPOptimizer(PeerNodeSettings ownSettings, PeerNodeSettings theirSettings) {
        this.ownSettings = ownSettings;
        this.theirSettings = theirSettings;
    }

    public String optimizeOwnSDP(String sdp) {
        //            SDPScheme sdpScheme = SDP.parse(description.getSdp());
//
//            for (SDPMedia m : sdpScheme.getMediaLevel()) {
//
//                // Disabling media streams
//                // m.setMode(SDPMediaMode.INACTIVE);
//
//                // Optimizing opus
//                if ("audio".equals(m.getType())) {
//                    for (SDPCodec codec : m.getCodecs()) {
//                        if ("opus".equals(codec.getName())) {
//                            codec.getFormat().put("maxcodedaudiobandwidth", "16000");
//                            codec.getFormat().put("maxaveragebitrate", "20000");
//                            codec.getFormat().put("stereo", "0");
//                            codec.getFormat().put("useinbandfec", "1");
//                            codec.getFormat().put("usedtx", "1");
//                        }
//                    }
//                }
//            }
//
//            return new WebRTCSessionDescription(description.getType(), sdpScheme.toSDP());
        return sdp;
    }

    public String optimizeTheirSDP(String sdp) {
        return sdp;
    }
}
