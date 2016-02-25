package im.actor.core.js.entity;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArray;

import im.actor.core.entity.Peer;
import im.actor.core.js.JsMessenger;
import im.actor.core.viewmodel.CallMember;
import im.actor.core.viewmodel.CallVM;

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
        return create(JsPeer.create(model.getPeer()), model.isOutgoing(), members, state);
    }

    public static native JsCall create(JsPeer peer, boolean isOutgoing, JsArray<JsPeerInfo> members, String state)/*-{
        return {peer: peer, isOutgoing: isOutgoing, members: members, state: state};
    }-*/;

    protected JsCall() {

    }
}
