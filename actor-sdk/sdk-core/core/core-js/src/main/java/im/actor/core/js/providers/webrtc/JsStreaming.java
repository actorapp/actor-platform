package im.actor.core.js.providers.webrtc;

import im.actor.core.js.modules.JsScheduller;
import im.actor.runtime.promise.Promise;
import im.actor.runtime.promise.PromiseFunc;
import im.actor.runtime.promise.PromiseResolver;

public class JsStreaming {

    public static Promise<JsUserMediaStream> getUserAudio() {
        return getUserMedia(JsUserMediaConstraint.audioOnly());
    }

    public static Promise<JsUserMediaStream> getUserVideo() {
        return getUserMedia(JsUserMediaConstraint.videoOnly());
    }

    public static Promise<JsUserMediaStream> getUserAudioVideo() {
        return getUserMedia(JsUserMediaConstraint.audioVideo());
    }

    public static Promise<JsUserMediaStream> getUserMedia(final JsUserMediaConstraint constraint) {
        return new Promise<>(new PromiseFunc<JsUserMediaStream>() {
            @Override
            public void exec(final PromiseResolver<JsUserMediaStream> resolver) {
                getUserMediaJs(constraint, new JsMediaCallback() {
                    @Override
                    public void onCreated(JsUserMediaStream mediaStream) {
                        resolver.result(mediaStream);
                    }

                    @Override
                    public void onError(JsUserMediaError error) {
                        resolver.error(new MediaException(error));
                    }
                });
            }
        }).done(JsScheduller.scheduller());
    }

    protected static native void getUserMediaJs(JsUserMediaConstraint constraint, JsMediaCallback callback)/*-{
        $wnd.navigator.getUserMedia(constraint, function(stream) {
            callback.@im.actor.core.js.providers.webrtc.JsMediaCallback::onCreated(*)(stream);
        }, function(error) {
            callback.@im.actor.core.js.providers.webrtc.JsMediaCallback::onError(*)(error);
        });
    }-*/;
}
