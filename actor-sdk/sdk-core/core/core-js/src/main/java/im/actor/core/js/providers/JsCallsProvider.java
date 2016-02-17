package im.actor.core.js.providers;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.media.client.Audio;

import im.actor.core.js.JsMessenger;
import im.actor.core.providers.CallsProvider;
import im.actor.core.viewmodel.CallState;

public class JsCallsProvider implements CallsProvider {

    private Audio callBeep;

    public JsCallsProvider() {
        callBeep = Audio.createIfSupported();
        if (callBeep != null) {
            callBeep.setLoop(true);
        }
    }

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
    public void onCallAnswered(long callId) {

    }

    @Override
    public void onCallEnd(long callId) {
        JsMessenger.getInstance().broadcastEvent("calls", callEvent("" + callId, "ended"));

        // Obsolete
        JsMessenger.getInstance().broadcastEvent("call", callEvent("" + callId, "ended"));
    }

    @Override
    public void startOutgoingBeep() {
        if (callBeep != null) {
            callBeep.setSrc("assets/sound/tone.mp3");
            callBeep.play();
        }
    }

    @Override
    public void stopOutgoingBeep() {
        if (callBeep != null) {
            callBeep.pause();
            callBeep.setSrc(null);
        }
    }

    private final native JavaScriptObject callEvent(String id, String type)/*-{
        return {id: id, type: type};
    }-*/;
}
