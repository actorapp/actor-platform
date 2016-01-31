package im.actor.runtime.actors.messages;

public class StashBegin {

    public static final StashBegin INSTANCE = new StashBegin();

    private StashBegin() {
    }

    @Override
    public String toString() {
        return "StashBegin";
    }
}
