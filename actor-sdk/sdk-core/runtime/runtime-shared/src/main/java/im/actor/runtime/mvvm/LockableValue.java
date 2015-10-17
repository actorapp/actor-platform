package im.actor.runtime.mvvm;

import java.util.HashSet;

import im.actor.runtime.annotations.MainThread;

public class LockableValue<T> extends Value<T> {

    private ValueChangedListener<T> listener = new ValueChangedListener<T>() {
        @Override
        public void onChanged(T val, Value<T> valueModel) {
            LockableValue.this.originalValue = val;

            if (!isLockEnabled) {
                notifyInMainThread(LockableValue.this.originalValue);
            }
        }
    };
    private Value<T> baseValue;

    private T originalValue;
    private T modifiedValue;

    private boolean isLockEnabled = false;
    private int NEXT_LOCK_ID = 0;
    private HashSet<Integer> activeLocks = new HashSet<Integer>();

    public LockableValue(String name, Value<T> baseValue) {
        super(name);
        this.baseValue = baseValue;
        this.originalValue = baseValue.get();

        baseValue.subscribe(listener);
    }

    @MainThread
    public int createLock() {
        // Check lock enable
        if (activeLocks.size() == 0) {
            modifiedValue = originalValue;
            isLockEnabled = true;
        }

        // Creating lock id
        int lockId = NEXT_LOCK_ID++;
        activeLocks.add(lockId);
        return lockId;
    }

    /**
     * Can be executed only between create/release lock calls
     */
    @MainThread
    public void change(T obj) {
        if (!isLockEnabled) {
            throw new RuntimeException("changing of values only cen be performed in locked state");
        }

        modifiedValue = obj;
        notifyInMainThread(modifiedValue);
    }

    @MainThread
    public void releaseLock(int id) {
        // Releasing lock
        activeLocks.remove(id);

        // Check lock disable
        if (activeLocks.size() == 0) {
            modifiedValue = null;
            isLockEnabled = false;
            notifyInMainThread(originalValue);
        }
    }

    @Override
    public T get() {
        if (isLockEnabled) {
            return modifiedValue;
        } else {
            return originalValue;
        }
    }

    protected T getModifiedValue() {
        return modifiedValue;
    }

    public void destroy() {
        baseValue.unsubscribe(listener);
    }
}
