package im.actor.runtime.function;

public class CancellableSimple implements Cancellable {

    private boolean isCancelled;

    @Override
    public boolean isCancelled() {
        return isCancelled;
    }

    @Override
    public void cancel() {
        isCancelled = true;
    }
}
