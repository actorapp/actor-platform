package im.actor.runtime.js.webrtc.js;

import com.google.gwt.core.client.JavaScriptObject;

public class JsUserMediaConstraint extends JavaScriptObject {

    public static native JsUserMediaConstraint create(boolean audio, boolean video)/*-{
        return {audio: audio, video: video};
    }-*/;

    public static native JsUserMediaConstraint audioOnly()/*-{
        return {audio: true, video: false};
    }-*/;

    public static native JsUserMediaConstraint videoOnly()/*-{
        return {audio: false, video: true};
    }-*/;

    public static native JsUserMediaConstraint audioVideo()/*-{
        return {audio: true, video: true};
    }-*/;

    protected JsUserMediaConstraint() {

    }
}
