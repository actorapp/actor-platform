package im.actor.runtime.function;

public class CountedReference<T extends Closable> {

    private T value;
    private int counter;
    private boolean isReleased;

    public CountedReference(T value) {
        this.value = value;
        this.isReleased = false;
        this.counter = 1;
    }

    public T get() {
        if (isReleased) {
            throw new RuntimeException("Already Released!");
        }
        return value;
    }

    public synchronized CountedReference<T> acquire() {
        if (isReleased) {
            throw new RuntimeException("Already Released!");
        }
        counter++;
        acquire(counter);
        return this;
    }

    protected synchronized void acquire(int counter) {

    }

    public synchronized void release() {
        counter--;
        release(counter);
        if (counter == 0) {
            if (isReleased) {
                throw new RuntimeException("Already Released!");
            }
            value.close();
            value = null;
            isReleased = true;
        }
    }

    protected synchronized void release(int counter) {

    }
}
