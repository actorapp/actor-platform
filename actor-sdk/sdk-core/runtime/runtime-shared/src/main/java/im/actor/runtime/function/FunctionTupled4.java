package im.actor.runtime.function;

public abstract class FunctionTupled4<T1, T2, T3, T4, R> implements Function<Tuple4<T1, T2, T3, T4>, R> {

    public abstract R apply(T1 t1, T2 t2, T3 t3, T4 t4);

    @Override
    public final R apply(Tuple4<T1, T2, T3, T4> tuple) {
        return apply(tuple.getT1(), tuple.getT2(), tuple.getT3(), tuple.getT4());
    }
}
