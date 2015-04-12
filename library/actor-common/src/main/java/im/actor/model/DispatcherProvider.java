package im.actor.model;

/**
 * Provider for executing callbacks
 */
public interface DispatcherProvider {

    /**
     * Dispatch Runnable on Callbacks thread
     *
     * @param runnable Runnable
     */
    public void dispatch(Runnable runnable);
}
