package com.droidkit.engine._internal.util;

import java.lang.ref.WeakReference;

public class WeakEqualReference<T> extends WeakReference<T> {

    public static final boolean USE_AS_STRONG_REFERENCE = true;

    private int cachedHashCode = 0;

    private T ref;

    public WeakEqualReference(T r) {
        super(r);
        if(USE_AS_STRONG_REFERENCE) {
            ref = r;
        }
    }

    @Override
    public T get() {
        if(USE_AS_STRONG_REFERENCE) {
            return ref;
        } else {
            return super.get();
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public boolean equals(Object other) {

        boolean returnValue = super.equals(other);

        // If we're not equal, then check equality using referenced objects
        if (!returnValue && (other instanceof com.droidkit.engine._internal.util.WeakEqualReference<?>)) {
            T value = this.get();
            if (null != value) {
                T otherValue = ((com.droidkit.engine._internal.util.WeakEqualReference<T>) other).get();

                // The delegate equals should handle otherValue == null
                returnValue = value.equals(otherValue);
            }
        }

        return returnValue;
    }

    @Override
    public int hashCode() {
        // The real hash code can be equals to zero as well, but there is nothing bad if we'll recalculate it
        // We can't removeItem WeakEqualReference from set if hash code was changed, see
        // http://stackoverflow.com/questions/254441/hashset-removeItem-and-iterator-removeItem-not-working
        if(cachedHashCode == 0) {
            T value = this.get();
            cachedHashCode  = value != null ? value.hashCode() : super.hashCode();
        }
        return cachedHashCode;
    }
}
