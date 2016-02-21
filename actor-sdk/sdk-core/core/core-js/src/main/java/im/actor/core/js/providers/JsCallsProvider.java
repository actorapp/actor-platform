package im.actor.core.js.providers;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.media.client.Audio;

import im.actor.core.js.JsMessenger;
import im.actor.core.providers.CallsProvider;
import im.actor.core.viewmodel.CallState;

public class JsCallsProvider implements CallsProvider {

    private Audio callBeep;
    private Audio callRingtone;

    public JsCallsProvider() {

        Audio callBeep = Audio.createIfSupported();
        if (callBeep != null) {
            if (!"".equals(callBeep.canPlayType("audio/ogg;"))) {
                callBeep.setSrc("assets/sound/tone.ogg");
            } else if (!"".equals(callBeep.canPlayType("audio/mpeg;"))) {
                callBeep.setSrc("assets/sound/tone.mp3");
            } else {
                // Not Supported
                return;
            }
            callBeep.setLoop(true);
            this.callBeep = callBeep;
        }

        Audio callRingtone = Audio.createIfSupported();
        if (callRingtone != null) {
            if (!"".equals(callRingtone.canPlayType("audio/ogg;"))) {
                callRingtone.setSrc("assets/sound/ringtone.ogg");
            } else if (!"".equals(callRingtone.canPlayType("audio/mpeg;"))) {
                callRingtone.setSrc("assets/sound/ringtone.mp3");
            } else {
                // Not Supported
                return;
            }
            callRingtone.setLoop(true);
            this.callRingtone = callRingtone;
        }
    }

    @Override
    public void onCallStart(long callId) {
        JsMessenger.getInstance().broadcastEvent("calls", callEvent("" + callId, "started"));

        if (!JsMessenger.getInstance().getCall(callId).isOutgoing()) {
            startRingtone();
        }
    }

    @Override
    public void onCallAnswered(long callId) {
        stopRingtone();
    }

    @Override
    public void onCallEnd(long callId) {
        JsMessenger.getInstance().broadcastEvent("calls", callEvent("" + callId, "ended"));

        stopRingtone();
    }

    @Override
    public void startOutgoingBeep() {
        if (callBeep != null) {
            callBeep.setCurrentTime(0);
            callBeep.play();
        }
    }

    @Override
    public void stopOutgoingBeep() {
        if (callBeep != null) {
            callBeep.pause();
            callBeep.setCurrentTime(0);
        }
    }

    private void startRingtone() {
        if (callRingtone != null) {
            callRingtone.setCurrentTime(0);
            callRingtone.play();
        }
    }

    private void stopRingtone() {
        if (callRingtone != null) {
            callRingtone.pause();
            callRingtone.setCurrentTime(0);
        }
    }

    private final native JavaScriptObject callEvent(String id, String type)/*-{
        return {id: id, type: type};
    }-*/;
}
