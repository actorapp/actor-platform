/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.core.viewmodel;

import com.google.j2objc.annotations.Property;

/**
 * User presence state
 */
public class UserPresence {
    @Property("nonatomic, readonly")
    private final State state;
    @Property("nonatomic, readonly")
    private final long lastSeen;

    /**
     * <p>INTERNAL API</p>
     * Create UserPresence
     *
     * @param state presence state
     */
    public UserPresence(State state) {
        this.state = state;
        this.lastSeen = 0;
    }

    /**
     * <p>INTERNAL API</p>
     * Create UserPresence
     *
     * @param state    presence state
     * @param lastSeen last seen
     */
    public UserPresence(State state, long lastSeen) {
        this.state = state;
        this.lastSeen = lastSeen;
    }

    /**
     * Get Presence state
     *
     * @return Presence state
     */
    public State getState() {
        return state;
    }

    /**
     * Get Last seen date
     *
     * @return last seen date in ms in unixtime
     */
    public long getLastSeen() {
        return lastSeen;
    }

    public enum State {
        UNKNOWN,
        ONLINE,
        OFFLINE
    }
}
