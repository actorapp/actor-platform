package im.actor.runtime.webrtc.sdp;

import java.util.ArrayList;
import java.util.List;

import im.actor.runtime.webrtc.sdp.entities.SDPMedia;
import im.actor.runtime.webrtc.sdp.entities.SDPSession;

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


    public String toSDP() {
        String res = sessionLevel.toSDP();
        for (SDPMedia media : mediaLevel) {
            res += media.toSDP();
        }
        return res;
    }

    @Override
    public String toString() {
        return toSDP();
    }
}
