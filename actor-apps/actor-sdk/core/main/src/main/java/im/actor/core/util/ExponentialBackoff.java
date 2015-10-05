/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.core.util;

import java.util.Random;

import im.actor.runtime.threading.AtomicIntegerCompat;

/**
 * Helper for calculation of exponential backoff delays
 */

public class ExponentialBackoff {

    private final int minDelay;
    private final int maxDelay;
    private final int maxFailureCount;

    public ExponentialBackoff(int minDelay,
                              int maxDelay,
                              int maxFailureCount) {
        this.minDelay = minDelay;
        this.maxDelay = maxDelay;
        this.maxFailureCount = maxFailureCount;
    }

    private final AtomicIntegerCompat currentFailureCount = im.actor.runtime.Runtime.createAtomicInt(1);

    private final Random random = new Random();

    /**
     * Calculating wait duration after failure attempt
     *
     * @return wait in ms
     */
    public long exponentialWait() {
        long maxDelayRet = minDelay + ((maxDelay - minDelay) / maxFailureCount) * currentFailureCount.get();
        synchronized (random) {
            return (long) (random.nextFloat() * maxDelayRet);
        }
    }

    /**
     * Notification about failure
     */
    public void onFailure() {
        final int val = currentFailureCount.incrementAndGet();
        if (val > maxFailureCount) {
            currentFailureCount.compareAndSet(val, maxFailureCount);
        }
    }

    /**
     * Notification about success
     */
    public void onSuccess() {
        reset();
    }

    /**
     * Resetting backoff object
     */
    public void reset() {
        currentFailureCount.set(0);
    }
}