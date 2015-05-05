/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.model.util;

import java.util.Random;

import im.actor.model.droidkit.actors.Environment;

/**
 * Helper for calculation of exponential backoff delays
 */
public class ExponentialBackoff {

    private static final int MIN_DELAY = 100;
    private static final int MAX_DELAY = 15000;
    private static final int MAX_FAILURE_COUNT = 50;

    private final AtomicIntegerCompat currentFailureCount = Environment.createAtomicInt(1);

    private final Random random = new Random();

    /**
     * Calculating wait duration after failure attempt
     *
     * @return wait in ms
     */
    public long exponentialWait() {
        long maxDelay = MIN_DELAY + ((MAX_DELAY - MIN_DELAY) / MAX_FAILURE_COUNT) * currentFailureCount.get();
        synchronized (random) {
            return (long) (random.nextFloat() * maxDelay);
        }
    }

    /**
     * Notification about failure
     */
    public void onFailure() {
        final int val = currentFailureCount.incrementAndGet();
        if (val > 50) {
            currentFailureCount.compareAndSet(val, MAX_FAILURE_COUNT);
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