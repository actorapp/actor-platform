package im.actor.core.webrtc;

import im.actor.core.entity.signals.AbsSignal;

/**
 * Controller for WebRTC calls
 */
public interface WebRTCController {

    /**
     * Answering current call
     */
    void answerCall();

    /**
     * Send Signaling to current call
     *
     * @param signal signal
     */
    void sendSignaling(AbsSignal signal);

    /**
     * Ending current call
     */
    void endCall();
}
