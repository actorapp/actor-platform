package im.actor.core.modules.events;

import im.actor.runtime.eventbus.Event;

public class ConnectingStateChanged extends Event {

    public static final String EVENT = "connecting_state_changed";

    private boolean isConnecting;

    public ConnectingStateChanged(boolean isConnecting) {
        this.isConnecting = isConnecting;
    }

    public boolean isConnecting() {
        return isConnecting;
    }

    @Override
    public String getType() {
        return EVENT;
    }

    @Override
    public String toString() {
        return EVENT + " {" + isConnecting + "}";
    }
}
