package im.actor.core.webrtc.sdp;

import java.util.ArrayList;
import java.util.List;

public class SDPScheme {

    public SDPSession sessionLevel;
    public ArrayList<SDPMedia> mediaLevel;

    public SDPScheme(SDPSession sessionLevel, List<SDPMedia> mediaLevel) {
        this.sessionLevel = sessionLevel;
        this.mediaLevel = new ArrayList<>(mediaLevel);
    }

    public SDPSession getSessionLevel() {
        return sessionLevel;
    }

    public ArrayList<SDPMedia> getMediaLevel() {
        return mediaLevel;
    }
}
