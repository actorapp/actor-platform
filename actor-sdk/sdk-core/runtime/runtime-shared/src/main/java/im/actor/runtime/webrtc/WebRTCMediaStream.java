package im.actor.runtime.webrtc;

import im.actor.runtime.function.Closable;

/**
 * WebRTC media stream
 */
public interface WebRTCMediaStream extends Closable {

    /**
     * Closing stream
     */
    void close();
}
