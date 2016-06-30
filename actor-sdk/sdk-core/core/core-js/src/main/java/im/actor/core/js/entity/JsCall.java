package im.actor.core.js.entity;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArray;

import java.util.ArrayList;

import im.actor.core.entity.Peer;
import im.actor.core.js.JsMessenger;
import im.actor.core.viewmodel.CallMember;
import im.actor.core.viewmodel.CallVM;
import im.actor.runtime.js.webrtc.MediaTrack;
import im.actor.runtime.js.webrtc.js.JsMediaStream;
import im.actor.runtime.js.webrtc.MediaStream;
import im.actor.runtime.js.webrtc.js.JsMediaStreamTrack;
import im.actor.runtime.webrtc.WebRTCMediaStream;
import im.actor.runtime.webrtc.WebRTCMediaTrack;

public class JsCall extends JavaScriptObject {

    public static JsCall create(JsMessenger messenger, CallVM model) {
        JsArray<JsPeerInfo> members = JsArray.createArray().cast();
        for (CallMember member : model.getMembers().get()) {
            members.push(messenger.buildPeerInfo(Peer.user(member.getUid())));
        }
        String state;
        switch (model.getState().get()) {
            case RINGING:
                state = "calling";
                break;
            case CONNECTING:
                state = "connecting";
                break;
            case IN_PROGRESS:
                state = "in_progress";
                break;
            default:
            case ENDED:
                state = "ended";
                break;
        }
        JsArray<JsMediaStreamTrack> tracks = JsArray.createArray().cast();
        for (WebRTCMediaTrack track : model.getTheirVideoTracks().get()) {
            tracks.push(((MediaTrack) track).getTrack());
        }
        ArrayList<WebRTCMediaTrack> ownTrack = model.getOwnVideoTracks().get();
        return create(JsPeer.create(model.getPeer()), model.isOutgoing(), members, state, !model.getIsAudioEnabled().get(), ownTrack.size() == 0 ? null : ((MediaTrack) ownTrack.get(0)).getTrack(), tracks);
    }

    public static native JsCall create(JsPeer peer, boolean isOutgoing, JsArray<JsPeerInfo> members, String state, boolean isMuted, JsMediaStreamTrack ownVideo, JsArray<JsMediaStreamTrack> tracks)/*-{
        return {peer: peer, isOutgoing: isOutgoing, members: members, state: state, isMuted: isMuted, ownVideo: ownVideo, tracks: tracks};
    }-*/;

    protected JsCall() {

    }
}
