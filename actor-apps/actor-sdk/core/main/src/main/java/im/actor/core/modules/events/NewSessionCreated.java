package im.actor.core.modules.events;

import im.actor.runtime.eventbus.Event;

public class NewSessionCreated extends Event {

    public static final String EVENT = "new_session";

    @Override
    public String getType() {
        return EVENT;
    }
}
