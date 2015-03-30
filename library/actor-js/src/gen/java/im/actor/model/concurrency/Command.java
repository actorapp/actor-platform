package im.actor.model.concurrency;

/**
 * Created by ex3ndr on 08.02.15.
 */
public interface Command<T> {
    public void start(CommandCallback<T> callback);
}
