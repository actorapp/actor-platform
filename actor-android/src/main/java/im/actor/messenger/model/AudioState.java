package im.actor.messenger.model;

/**
 * Created by ex3ndr on 02.10.14.
 */
public class AudioState {
    private State state;
    private int progress;

    public AudioState(State state, int progress) {
        this.state = state;
        this.progress = progress;
    }

    public AudioState(State state) {
        this.state = state;
    }

    public State getState() {
        return state;
    }

    public int getProgress() {
        return progress;
    }

    public static enum State {
        STOPPED, PLAYING, PAUSED
    }
}
