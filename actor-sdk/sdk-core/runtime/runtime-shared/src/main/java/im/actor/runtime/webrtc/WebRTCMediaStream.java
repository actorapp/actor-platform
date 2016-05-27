package im.actor.runtime.webrtc;

/**
 * WebRTC media stream
 */
public interface WebRTCMediaStream {

    /**
     * Is Audio Track enabled
     *
     * @return is track enabled
     */
    boolean isAudioEnabled();

    /**
     * Enabling/Disabling audio track
     *
     * @param isEnabled is track enabled
     */
    void setAudioEnabled(boolean isEnabled);

    /**
     * Is Video Track enabled
     *
     * @return is track enabled
     */
    boolean isVideoEnabled();


    /**
     * Enabling/Disabling video track
     *
     * @param isEnabled is track enabled
     */
    void setVideoEnabled(boolean isEnabled);

    /**
     * Closing stream
     */
    void close();
}
