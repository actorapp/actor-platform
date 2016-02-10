package im.actor.core.webrtc;

import org.jetbrains.annotations.NotNull;

/**
 * Controller for WebRTC calls
 */
public interface WebRTCController {

    /**
     * Answering current call
     */
    void answerCall();


    /**
     * Send candidate to Peer
     *
     * @param label label of candidate
     * @param id    id of candidate
     * @param sdp   sdp of candidate
     */
    void sendCandidate(int label, @NotNull String id, @NotNull String sdp);

    /**
     * Send calling offer
     *
     * @param sdp sdp of the offer
     */
    void sendOffer(@NotNull String sdp);

    /**
     * Send answer for offer
     *
     * @param sdp sdp of the offer
     */
    void sendAnswer(@NotNull String sdp);

    /**
     * Notify that engine is ready to receive candidates
     */
    void readyForCandidates();



    /**
     * Ending current call
     */
    void endCall();
}
