package im.actor.runtime.webrtc;

/**
 * WebRTC media stream
 */
public interface WebRTCMediaStream {

    /**
     * Is Track enabled
     *
     * @return is track enabled
     */
    boolean isEnabled();

    /**
     * Enabling/Disabling track
     *
     * @param isEnabled is track enabled
     */
    void setEnabled(boolean isEnabled);

    /**
     * Closing stream
     */
    void close();
}
