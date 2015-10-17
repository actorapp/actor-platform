package im.actor.core.modules.events;

import im.actor.runtime.eventbus.Event;

public class UserVisible extends Event {
    
    public static final String EVENT = "user_visible";

    private int uid;

    public UserVisible(int uid) {
        this.uid = uid;
    }

    public int getUid() {
        return uid;
    }

    @Override
    public String getType() {
        return EVENT;
    }

    @Override
    public String toString() {
        return EVENT + " {" + uid + "}";
    }
}
