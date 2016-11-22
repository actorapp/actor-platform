package im.actor.runtime.function;

public interface Cancellable {

    boolean isCancelled();

    void cancel();
}
