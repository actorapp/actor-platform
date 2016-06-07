package im.actor.core.js.entity;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArray;

import im.actor.core.entity.Peer;
import im.actor.core.js.JsMessenger;
import im.actor.core.viewmodel.CallMember;
import im.actor.core.viewmodel.CallVM;
import im.actor.runtime.js.webrtc.JsMediaStream;
import im.actor.runtime.js.webrtc.MediaStream;
import im.actor.runtime.webrtc.WebRTCMediaStream;

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
        JsArray<JsMediaStream> streams = JsArray.createArray().cast();
        for (WebRTCMediaStream stream : model.getMediaStreams().get()) {
            streams.push(((MediaStream) stream).getStream());
        }
        return create(JsPeer.create(model.getPeer()), model.isOutgoing(), members, state, model.getIsMuted().get(), ((MediaStream) model.getOwnMediaStream().get()).getStream(), streams);
    }

    public static native JsCall create(JsPeer peer, boolean isOutgoing, JsArray<JsPeerInfo> members, String state, boolean isMuted, JsMediaStream ownStream, JsArray<JsMediaStream> streams)/*-{
        return {peer: peer, isOutgoing: isOutgoing, members: members, state: state, isMuted: isMuted, ownStream: ownStream, streams: streams};
    }-*/;

    protected JsCall() {

    }
}
