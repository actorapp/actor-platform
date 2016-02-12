package im.actor.runtime.threading;

public interface Dispatcher {
    void dispatch(Runnable message, long delay);
}