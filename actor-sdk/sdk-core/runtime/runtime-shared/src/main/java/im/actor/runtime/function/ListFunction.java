package im.actor.runtime.function;

import java.util.List;

public interface ListFunction<T, R> {
    R apply(List<T> t);
}
