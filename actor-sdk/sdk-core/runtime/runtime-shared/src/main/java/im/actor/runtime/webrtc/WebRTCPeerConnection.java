package im.actor.runtime.webrtc;

import com.google.j2objc.annotations.ObjectiveCName;

import org.jetbrains.annotations.NotNull;

import im.actor.runtime.actors.messages.Void;
import im.actor.runtime.function.Closable;
import im.actor.runtime.function.CountedReference;
import im.actor.runtime.promise.Promise;

/**
 * WebRTC peer connection
 */
public interface WebRTCPeerConnection {

    /**
     * Adding Callback for events from peer connection
     *
     * @param callback callback for adding
     */
    @ObjectiveCName("addCallback:")
    void addCallback(@NotNull WebRTCPeerConnectionCallback callback);

    /**
     * Remove Callback for events from peer connection
     *
     * @param callback callback for adding
     */
    @ObjectiveCName("removeCallback:")
    void removeCallback(@NotNull WebRTCPeerConnectionCallback callback);

    /**
     * Adding Remote Candidate
     *
     * @param index index of candidate
     * @param id    id of candidate
     * @param sdp   candidate SDP
     */
    @ObjectiveCName("addCandidateWithIndex:withId:withSDP:")
    void addCandidate(int index, @NotNull String id, @NotNull String sdp);

    /**
     * Adding Own Stream
     *
     * @param stream added stream
     */
    @ObjectiveCName("addOwnStream:")
    void addOwnStream(@NotNull CountedReference<WebRTCMediaStream> stream);


    /**
     * Removing Own Stream
     *
     * @param stream removed stream
     */
    @ObjectiveCName("removeOwnStream:")
    void removeOwnStream(@NotNull CountedReference<WebRTCMediaStream> stream);

    /**
     * Setting Local Description
     *
     * @param description description to set
     * @return Promise of set description. MUST be same as argument.
     */
    @NotNull
    @ObjectiveCName("setLocalDescription:")
    Promise<WebRTCSessionDescription> setLocalDescription(@NotNull WebRTCSessionDescription description);

    /**
     * Setting Remote Description
     *
     * @param description description to set
     * @return Promise of set description. MUST be same as argument.
     */
    @NotNull
    @ObjectiveCName("setRemoteDescription:")
    Promise<WebRTCSessionDescription> setRemoteDescription(@NotNull WebRTCSessionDescription description);

    /**
     * Create Offer
     *
     * @return Promise of created offer
     */
    @NotNull
    @ObjectiveCName("creteOffer")
    Promise<WebRTCSessionDescription> createOffer();

    /**
     * Create Answer
     *
     * @return Promise of created answer
     */
    @NotNull
    @ObjectiveCName("createAnswer")
    Promise<WebRTCSessionDescription> createAnswer();

    /**
     * Closing peer connection
     */
    @ObjectiveCName("close")
    void close();
}
