package im.actor.runtime.actors;

import org.jetbrains.annotations.NotNull;

import im.actor.runtime.actors.messages.Void;

public abstract class Future {

    private Object result;
    private Exception exception;
    private boolean isFinished;

    public void onResult() {
        onResult(Void.INSTANCE);
    }

    public void onResult(@NotNull Object result) {
        if (isFinished) {
            throw new RuntimeException("Already finished!");
        }
        this.isFinished = true;
        this.result = result;
        deliverResult();
    }

    public void onError(@NotNull Exception e) {
        if (isFinished) {
            throw new RuntimeException("Already finished!");
        }
        this.isFinished = true;
        this.exception = e;
        deliverResult();
    }

    protected abstract void deliverResult();

    public boolean isFinished() {
        return isFinished;
    }

    public boolean isSuccess() {
        return isFinished && exception == null;
    }

    public Object getResult() {
        return result;
    }

    public Exception getException() {
        return exception;
    }
}