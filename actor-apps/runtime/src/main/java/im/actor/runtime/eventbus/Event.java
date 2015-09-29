package im.actor.runtime.eventbus;

public abstract class Event {

    public abstract String getType();

    @Override
    public String toString() {
        return getType();
    }
}
