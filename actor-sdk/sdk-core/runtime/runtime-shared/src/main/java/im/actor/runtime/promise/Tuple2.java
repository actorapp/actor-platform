package im.actor.runtime.promise;

import com.google.j2objc.annotations.Property;

import org.jetbrains.annotations.Nullable;

public class Tuple2<T1, T2> {

    @Nullable
    @Property("readonly, nonatomic")
    private final T1 t1;
    @Nullable
    @Property("readonly, nonatomic")
    private final T2 t2;

    public Tuple2(@Nullable T1 t1, @Nullable T2 t2) {
        this.t1 = t1;
        this.t2 = t2;
    }

    @Nullable
    public T1 getT1() {
        return t1;
    }

    @Nullable
    public T2 getT2() {
        return t2;
    }
}
