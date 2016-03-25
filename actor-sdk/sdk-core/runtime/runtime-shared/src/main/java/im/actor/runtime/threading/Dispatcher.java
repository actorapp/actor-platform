package im.actor.runtime.threading;

public interface Dispatcher {
    DispatchCancel dispatch(Runnable message, long delay);
}