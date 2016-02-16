package im.actor.core.js.providers;

import com.google.gwt.core.client.JavaScriptObject;

import im.actor.core.js.JsMessenger;
import im.actor.core.providers.CallsProvider;
import im.actor.core.viewmodel.CallState;

public class JsCallsProvider implements CallsProvider {

    @Override
    public void onCallStart(long callId) {
        JsMessenger.getInstance().broadcastEvent("calls", callEvent("" + callId, "started"));

        // Obsolete
        if (JsMessenger.getInstance().getCall(callId).getState().get() == CallState.CALLING_OUTGOING) {
            JsMessenger.getInstance().broadcastEvent("call", callEvent("" + callId, "ongoing"));
        } else {
            JsMessenger.getInstance().broadcastEvent("call", callEvent("" + callId, "incoming"));
        }
    }

    @Override
    public void onCallEnd(long callId) {
        JsMessenger.getInstance().broadcastEvent("calls", callEvent("" + callId, "ended"));

        // Obsolete
        JsMessenger.getInstance().broadcastEvent("call", callEvent("" + callId, "ended"));
    }

    private final native JavaScriptObject callEvent(String id, String type)/*-{
        return {id: id, type: type};
    }-*/;
}
