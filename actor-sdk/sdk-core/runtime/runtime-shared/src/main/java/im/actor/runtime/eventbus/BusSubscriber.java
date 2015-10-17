package im.actor.runtime.eventbus;

public interface BusSubscriber {
    /**
     * Handling event. Unsubscribing during calling this method is unsupported.
     *
     * @param event event value
     */
    void onBusEvent(Event event);
}
