package im.actor.model.droidkit.actors.dispatch;

/**
 * Used as callback for message processing
 */
public interface Dispatch<T> {
    void dispatchMessage(T message);
}
