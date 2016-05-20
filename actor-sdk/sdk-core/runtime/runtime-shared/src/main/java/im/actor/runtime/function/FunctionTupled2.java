package im.actor.runtime.function;

public abstract class FunctionTupled2<T1, T2, R> implements Function<Tuple2<T1, T2>, R> {

    public abstract R apply(T1 t1, T2 t2);

    @Override
    public final R apply(Tuple2<T1, T2> t1T2Tuple2) {
        return apply(t1T2Tuple2.getT1(), t1T2Tuple2.getT2());
    }
}
