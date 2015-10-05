package im.actor.core.modules.events;

import im.actor.runtime.eventbus.Event;

public class AppVisibleChanged extends Event {

    public static final String EVENT = "app_visible_changed";

    private boolean isVisible;

    public AppVisibleChanged(boolean isVisible) {
        this.isVisible = isVisible;
    }

    public boolean isVisible() {
        return isVisible;
    }

    @Override
    public String getType() {
        return EVENT;
    }

    @Override
    public String toString() {
        return EVENT + " {" + isVisible + "}";
    }
}
