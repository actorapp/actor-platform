package im.actor.runtime.function;

import com.google.j2objc.annotations.ObjectiveCName;

public interface Function<T, R> {
    R apply(T t);
}
