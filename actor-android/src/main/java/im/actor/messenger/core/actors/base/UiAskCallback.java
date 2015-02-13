package im.actor.messenger.core.actors.base;

/**
 * Created by ex3ndr on 08.10.14.
 */
public interface UiAskCallback<V> {
    public void onPreStart();

    public void onCompleted(V res);

    public void onError(Throwable t);
}
