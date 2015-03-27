package im.actor.model.viewmodel;

/**
 * Created by ex3ndr on 19.02.15.
 */
public class UserPresence {
    private final State state;
    private final long lastSeen;

    public UserPresence(State state) {
        this.state = state;
        this.lastSeen = 0;
    }

    public UserPresence(State state, long lastSeen) {
        this.state = state;
        this.lastSeen = lastSeen;
    }

    public State getState() {
        return state;
    }

    public long getLastSeen() {
        return lastSeen;
    }

    public enum State {
        UNKNOWN,
        ONLINE,
        OFFLINE
    }
}
