package im.actor.runtime.actors;

import org.jetbrains.annotations.NotNull;

public abstract class Future<T> {

    private T result;
    private Exception exception;
    private boolean isFinished;

    public void onResult(@NotNull T result) {
        if (isFinished) {
            throw new RuntimeException("Already finished!");
        }
        this.isFinished = true;
        this.result = result;
        deliverResult();
    }

    public void onError(@NotNull Exception e){
        if (isFinished) {
            throw new RuntimeException("Already finished!");
        }
        this.isFinished = true;
        this.exception = e;
    }

    protected abstract void deliverResult();

    public boolean isFinished() {
        return isFinished;
    }

    public boolean isSuccess() {
        return isFinished && exception != null;
    }

    public T getResult() {
        return result;
    }

    public Exception getException() {
        return exception;
    }
}