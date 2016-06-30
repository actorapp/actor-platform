package im.actor.runtime.webrtc;

import im.actor.runtime.function.Closable;

/**
 * WebRTC media stream
 */
public interface WebRTCMediaStream extends Closable {

    /**
     * Get Audio tracks of a stream
     *
     * @return Get Audio tracks of a stream
     */
    WebRTCMediaTrack[] getAudioTracks();

    /**
     * Get Video tracks of a stream
     *
     * @return Get Video tracks of a stream
     */
    WebRTCMediaTrack[] getVideoTracks();

    /**
     * Get tracks of a stream
     *
     * @return Get tracks of a stream
     */
    WebRTCMediaTrack[] getTracks();

    /**
     * Closing stream
     */
    void close();
}
