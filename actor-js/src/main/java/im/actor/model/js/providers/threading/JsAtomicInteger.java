package im.actor.model.js.providers.threading;

import im.actor.model.util.AtomicIntegerCompat;

/**
 * Created by ex3ndr on 21.02.15.
 */
public class JsAtomicInteger extends AtomicIntegerCompat {

    private int value;

    public JsAtomicInteger(int value) {
        this.value = value;
    }

    @Override
    public int get() {
        return value;
    }

    @Override
    public int incrementAndGet() {
        return ++value;
    }

    @Override
    public int getAndIncrement() {
        return value++;
    }

    @Override
    public void compareAndSet(int exp, int v) {
        if (this.value == exp) {
            this.value = v;
        }
    }

    @Override
    public void set(int v) {
        this.value = v;
    }
}
