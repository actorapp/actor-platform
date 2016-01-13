package im.actor.core.modules.events;

import im.actor.runtime.eventbus.Event;

public class IncomingCall extends Event {

    public static final String EVENT = "incoming_call";

    private long callId;

    public IncomingCall(long callId) {
        this.callId = callId;
    }

    public long getCall() {
        return callId;
    }

    @Override
    public String getType() {
        return EVENT;
    }
}
