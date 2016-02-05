package im.actor.core;

public interface WebRTCProvider {

    void onIncomingCall();

    void onOutgoingCall();

    void onCallStarted();

    void onSignalingReceived(byte[] data);

    void onCallEnded();
}