package im.actor.model;

/**
 * Created by ex3ndr on 23.03.15.
 */
public interface DispatcherProvider {
    public void dispatch(Runnable runnable);
}
