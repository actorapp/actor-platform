package im.actor.runtime.webrtc;

import com.google.j2objc.annotations.ObjectiveCName;

import org.jetbrains.annotations.NotNull;

import im.actor.runtime.promise.Promise;

public interface WebRTCPeerConnection {

    @ObjectiveCName("addCallback:")
    void addCallback(@NotNull WebRTCPeerConnectionCallback callback);

    @ObjectiveCName("removeCallback:")
    void removeCallback(@NotNull WebRTCPeerConnectionCallback callback);

    @ObjectiveCName("addCandidateWithLabel:withId:withCandidate:")
    void addCandidate(int label, @NotNull String id, @NotNull String candidate);

    @ObjectiveCName("addOwnStream:")
    void addOwnStream(@NotNull WebRTCLocalStream stream);

    @ObjectiveCName("setLocalDescriptionWithType:withSPD:")
    @NotNull
    Promise<Boolean> setLocalDescription(@NotNull String type, @NotNull String sdp);

    @ObjectiveCName("setRemoteDescriptionWithType:withSPD:")
    @NotNull
    Promise<Boolean> setRemoteDescription(@NotNull String type, @NotNull String sdp);

    @ObjectiveCName("creteOffer")
    @NotNull
    Promise<String> createOffer();

    @ObjectiveCName("createAnswer")
    @NotNull
    Promise<String> createAnswer();

    @ObjectiveCName("close")
    void close();
}
