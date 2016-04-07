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

    private final Random random = new Random();

    private final int minDelay;
    private final int maxDelay;
    private final int maxFailureCount;

    private int currentFailureCount = 1;

    public ExponentialBackoff() {
        this(500, 15000, 50);
    }

    public ExponentialBackoff(int minDelay,
                              int maxDelay,
                              int maxFailureCount) {
        this.minDelay = minDelay;
        this.maxDelay = maxDelay;
        this.maxFailureCount = maxFailureCount;
    }

    /**
     * Calculating wait duration after failure attempt
     *
     * @return wait in ms
     */
    public synchronized long exponentialWait() {
        long maxDelayRet = minDelay + ((maxDelay - minDelay) / maxFailureCount) * currentFailureCount;
        return (long) (random.nextFloat() * maxDelayRet);
    }

    /**
     * Notification about failure
     */
    public synchronized void onFailure() {
        currentFailureCount++;
        if (currentFailureCount > maxFailureCount) {
            currentFailureCount = maxFailureCount;
        }
    }

    /**
     * Notification about success
     */
    public synchronized void onSuccess() {
        reset();
    }

    /**
     * Resetting backoff object
     */
    public synchronized void reset() {
        currentFailureCount = 0;
    }
}