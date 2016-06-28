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
     * Enable/disable audio tracks of a stream
     *
     * @param enable true to enable tracks
     */
    void setAudioTracksEnabled(boolean enable);

    /**
     * Enable/disable video tracks of a stream
     *
     * @param enable true to enable tracks
     */
    void setVideoTracksEnabled(boolean enable);

    /**
     * Closing stream
     */
    void close();
}
