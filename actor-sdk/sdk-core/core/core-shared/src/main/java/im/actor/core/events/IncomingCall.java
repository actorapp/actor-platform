package im.actor.core.events;

import im.actor.runtime.eventbus.Event;

public class IncomingCall extends Event {

    public static final String EVENT = "incoming_call";

    private long callId;
    private int uid;

    public IncomingCall(long callId, int uid) {
        this.callId = callId;
        this.uid = uid;
    }

    public long getCall() {
        return callId;
    }

    public int getUid() {
        return uid;
    }

    @Override
    public String getType() {
        return EVENT;
    }
}
