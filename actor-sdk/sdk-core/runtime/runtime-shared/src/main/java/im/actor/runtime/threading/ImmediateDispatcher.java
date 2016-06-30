package im.actor.runtime.threading;

public interface ImmediateDispatcher {
    void dispatchNow(Runnable runnable);
}
