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
    void addOwnStream(@NotNull WebRTCMediaStream stream);

    @NotNull
    @ObjectiveCName("setLocalDescription:")
    Promise<WebRTCSessionDescription> setLocalDescription(@NotNull WebRTCSessionDescription description);

    @NotNull
    @ObjectiveCName("setRemoteDescription:")
    Promise<WebRTCSessionDescription> setRemoteDescription(@NotNull WebRTCSessionDescription description);

    @NotNull
    @ObjectiveCName("creteOffer")
    Promise<WebRTCSessionDescription> createOffer();

    @NotNull
    @ObjectiveCName("createAnswer")
    Promise<WebRTCSessionDescription> createAnswer();

    @ObjectiveCName("close")
    void close();
}
