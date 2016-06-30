package im.actor.core.events;

import im.actor.runtime.eventbus.Event;

public class SettingsChanged extends Event {

    public static final String EVENT = "settings_changed";

    @Override
    public String getType() {
        return EVENT;
    }
}
