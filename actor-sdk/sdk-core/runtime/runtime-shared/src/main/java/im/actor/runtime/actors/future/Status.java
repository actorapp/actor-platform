package im.actor.runtime.actors.future;

public final class Status {
    public static <T> Future<T> Ok(T obj) {
        Future<T> res = new Future<T>();
        res.onResult(obj);
        return res;
    }
}
