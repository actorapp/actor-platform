package im.actor.core.js.providers.webrtc;

public class JsAudio {
    public static native void playStream(JsUserMediaStream stream)/*-{
        var audio = $wnd.document.createElement('AUDIO');
        $wnd.console.warn(audio);
        var url = URL.createObjectURL(stream);;
        $wnd.console.warn(url);
        audio.src = URL.createObjectURL(stream);
        audio.play();
    }-*/;
}
