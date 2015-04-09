package im.actor.model;

/**
 * Provider for dispatching on Main application Thread
 */
public interface MainThreadProvider {
    /**
     * Post Runnable to main thread.
     * Implementation is recommended to always post to main thread
     * also in cases when method is called from main thread
     *
     * @param runnable Runnable to execute
     */
    public void postToMainThread(Runnable runnable);

    /**
     * Is current thread is main thread
     *
     * @return is main thread
     */
    public boolean isMainThread();

    /**
     * Is current environment is single threaded (like javascript)
     *
     * @return is single threaded
     */
    public boolean isSingleThread();
}
