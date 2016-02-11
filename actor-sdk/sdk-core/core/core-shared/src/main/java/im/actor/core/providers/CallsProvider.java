package im.actor.core.providers;

import com.google.j2objc.annotations.ObjectiveCName;

import org.jetbrains.annotations.NotNull;

import im.actor.core.Messenger;

/**
 * WebRTC provider. Used for providing Calls support.
 * All methods except init are called in background call management actor.
 * IMPORTANT: Right after "onCallEnd" called you need to stop sending any signaling messages.
 * Between onIncomingCall/onOutgoingCall and onCallEnd all methods are called with the same call id.
 */
public interface CallsProvider {

    /**
     * Incoming Call event. To answer call invoke controller.answerCall();
     *
     * @param callId Unique Call Id
     */
    @ObjectiveCName("onIncomingCallWithCallId:")
    void onIncomingCall(long callId);

    /**
     * Outgoing Call event. This doesn't mean that call is started.
     *
     * @param callId Unique Call Id
     */
    @ObjectiveCName("onOutgoingCallWithCallId:")
    void onOutgoingCall(long callId);

    /**
     * Call End event
     *
     * @param callId Unique Call Id
     */
    @ObjectiveCName("onCallEndWithCallId:")
    void onCallEnd(long callId);
}