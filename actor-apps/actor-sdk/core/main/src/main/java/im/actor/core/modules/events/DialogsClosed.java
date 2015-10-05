package im.actor.core.modules.events;

import im.actor.runtime.eventbus.Event;

public class DialogsClosed extends Event {

    public static final String EVENT = "dialogs_closed";

    @Override
    public String getType() {
        return EVENT;
    }
}
