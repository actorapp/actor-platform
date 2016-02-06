package im.actor.core.js.providers.webrtc;

public class JsAudio {
    public static native void playStream(JsUserMediaStream stream)/*-{
        var audio = $wnd.document.createElement('audio');
        audio.src = URL.createObjectURL(stream);
    }-*/;
}
