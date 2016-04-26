package im.actor.runtime.function;

import com.google.j2objc.annotations.Property;

import org.jetbrains.annotations.Nullable;

public class Tuple2<T1, T2> {

    @Property("readonly, nonatomic")
    private final T1 t1;
    @Property("readonly, nonatomic")
    private final T2 t2;

    public Tuple2(@Nullable T1 t1, @Nullable T2 t2) {
        this.t1 = t1;
        this.t2 = t2;
    }

    public T1 getT1() {
        return t1;
    }

    public T2 getT2() {
        return t2;
    }
}
