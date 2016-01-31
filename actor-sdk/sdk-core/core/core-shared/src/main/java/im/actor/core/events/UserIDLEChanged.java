package im.actor.core.events;

import im.actor.runtime.eventbus.Event;

public class UserIDLEChanged extends Event {

    public static final String EVENT = "user_idle_changed";

    private boolean isIDLE;

    public UserIDLEChanged(boolean isIDLE) {
        this.isIDLE = isIDLE;
    }


    public boolean isIDLE() {
        return isIDLE;
    }

    @Override
    public String getType() {
        return EVENT;
    }

    @Override
    public String toString() {
        return EVENT + " {" + isIDLE + "}";
    }
}
