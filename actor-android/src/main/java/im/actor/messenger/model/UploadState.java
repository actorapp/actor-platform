package im.actor.messenger.model;

/**
 * Created by ex3ndr on 26.10.14.
 */
public class UploadState {

    private State state;
    private int progress;

    public UploadState(State state, int progress) {
        this.state = state;
        this.progress = progress;
    }

    public UploadState(State state) {
        this.state = state;
    }

    public State getState() {
        return state;
    }

    public int getProgress() {
        return progress;
    }

    public enum State {
        NONE,
        UPLOADING,
        UPLOADED
    }
}
