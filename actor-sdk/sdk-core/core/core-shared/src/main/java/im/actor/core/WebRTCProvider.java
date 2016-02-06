package im.actor.core;

import im.actor.core.entity.signals.AbsSignal;

public interface WebRTCProvider {

    void onIncomingCall();

    void onOutgoingCall();

    void onCallStarted();

    void onSignalingReceived(AbsSignal signal);

    void onCallEnded();
}