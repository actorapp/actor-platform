package im.actor.model.concurrency;

/**
 * Created by ex3ndr on 08.02.15.
 */
public interface CommandCallback<T> {
    public void onResult(T res);

    public void onError(Exception e);
}
