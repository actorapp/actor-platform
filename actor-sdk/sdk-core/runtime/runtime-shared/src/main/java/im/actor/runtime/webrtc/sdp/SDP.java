package im.actor.runtime.webrtc.sdp;

public abstract class SDP {
    public static SDPScheme parse(String sdp) {
        String[] lines = sdp.split("\\r?\\n");
        if (!lines[0].startsWith("v=")){
            throw new RuntimeException("First line doesn't start with version of SDP");
        }
        return null;
    }
}