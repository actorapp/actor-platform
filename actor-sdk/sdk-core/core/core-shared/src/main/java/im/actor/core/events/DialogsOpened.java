package im.actor.core.events;

import im.actor.runtime.eventbus.Event;

public class DialogsOpened extends Event {
    public static final String EVENT = "dialogs_opened";

    @Override
    public String getType() {
        return EVENT;
    }
}
