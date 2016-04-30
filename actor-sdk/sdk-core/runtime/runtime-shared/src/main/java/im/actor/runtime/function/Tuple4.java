package im.actor.runtime.function;

import com.google.j2objc.annotations.Property;

import org.jetbrains.annotations.Nullable;

public class Tuple4<T1, T2, T3, T4> {

    @Nullable
    @Property("readonly, nonatomic")
    private final T1 t1;
    @Nullable
    @Property("readonly, nonatomic")
    private final T2 t2;
    @Nullable
    @Property("readonly, nonatomic")
    private final T3 t3;
    @Nullable
    @Property("readonly, nonatomic")
    private final T4 t4;

    public Tuple4(@Nullable T1 t1, @Nullable T2 t2, @Nullable T3 t3, @Nullable T4 t4) {
        this.t1 = t1;
        this.t2 = t2;
        this.t3 = t3;
        this.t4 = t4;
    }

    @Nullable
    public T1 getT1() {
        return t1;
    }

    @Nullable
    public T2 getT2() {
        return t2;
    }

    @Nullable
    public T3 getT3() {
        return t3;
    }

    @Nullable
    public T4 getT4() {
        return t4;
    }
}
