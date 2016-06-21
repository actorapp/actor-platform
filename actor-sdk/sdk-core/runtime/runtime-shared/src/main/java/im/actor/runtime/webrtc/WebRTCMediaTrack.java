package im.actor.runtime.webrtc;

/**
 * WebRTC Media Track
 */
public interface WebRTCMediaTrack {

    /**
     * Get Track Type
     *
     * @return track type
     */
    int getTrackType();

    /**
     * Is Track Enabled
     *
     * @return is track enabled
     */
    boolean isEnabled();

    /**
     * Enable/disable track
     *
     * @param isEnabled is track enabled
     */
    void setEnabled(boolean isEnabled);
}
