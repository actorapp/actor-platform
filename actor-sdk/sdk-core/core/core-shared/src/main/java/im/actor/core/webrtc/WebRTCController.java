package im.actor.core.webrtc;

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
    void sendCandidate(int label, String id, String sdp);

    /**
     * Send calling offer
     *
     * @param sdp sdp of the offer
     */
    void sendOffer(String sdp);

    /**
     * Send answer for offer
     *
     * @param sdp sdp of the offer
     */
    void sendAnswer(String sdp);

    /**
     * Notify that engine is ready to receive candidates
     */
    void readyForCandidates();



    /**
     * Ending current call
     */
    void endCall();
}
