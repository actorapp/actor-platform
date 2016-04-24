package im.actor.runtime.function;

public abstract class ConsumerTupled4<T1, T2, T3, T4> implements Consumer<Tuple4<T1, T2, T3, T4>> {

    public abstract void apply(T1 t1, T2 t2, T3 t3, T4 t4);

    @Override
    public void apply(Tuple4<T1, T2, T3, T4> tuple) {
        apply(tuple.getT1(), tuple.getT2(), tuple.getT3(), tuple.getT4());
    }
}
