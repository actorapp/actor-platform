package im.actor.core.js.entity;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArray;

import im.actor.core.entity.Peer;
import im.actor.core.js.JsMessenger;
import im.actor.core.viewmodel.CallModel;

public class JsCall extends JavaScriptObject {

    public static JsCall create(JsMessenger messenger, CallModel model) {
        JsArray<JsPeerInfo> members = JsArray.createArray().cast();
        for (int uid : model.getActiveMembers().get()) {
            members.push(messenger.buildPeerInfo(Peer.user(uid)));
        }
        String state;
        switch (model.getState().get()) {
            case CALLING_INCOMING:
                state = "calling_in";
                break;
            case CALLING_OUTGOING:
                state = "calling_out";
                break;
            case IN_PROGRESS:
                state = "in_progress";
                break;
            default:
            case ENDED:
                state = "ended";
                break;
        }
        return create(JsPeer.create(model.getPeer()), members, state);
    }

    public static native JsCall create(JsPeer peer, JsArray<JsPeerInfo> members, String state)/*-{
        return {peer: peer, members: members, state: state};
    }-*/;

    protected JsCall() {

    }
}
