package im.actor.core.modules.calls.peers;

import im.actor.runtime.webrtc.WebRTCSessionDescription;
import im.actor.runtime.webrtc.sdp.SDP;
import im.actor.runtime.webrtc.sdp.SDPScheme;
import im.actor.runtime.webrtc.sdp.entities.SDPCodec;
import im.actor.runtime.webrtc.sdp.entities.SDPMedia;

public final class SDPOptimizer {

    public static WebRTCSessionDescription optimize(WebRTCSessionDescription src) {
        SDPScheme sdpScheme = SDP.parse(src.getSdp());

        // Prefer ISAC over other audio codecs
        for (SDPMedia media : sdpScheme.getMediaLevel()) {
            SDPCodec isacCodec = null;
            for (SDPCodec codec : media.getCodecs()) {
                if (codec.getName().toLowerCase().equals("isac")) {
                    isacCodec = codec;
                    break;
                }
            }
            if (isacCodec != null) {
                media.getCodecs().remove(isacCodec);
                media.getCodecs().add(0, isacCodec);
            }
        }

        return new WebRTCSessionDescription(src.getType(), sdpScheme.toSDP());
    }

    private SDPOptimizer() {

    }
}
