package im.actor.runtime.actors.messages;

public class StashEnd {
    public static final StashEnd INSTANCE = new StashEnd();

    private StashEnd() {
    }

    @Override
    public String toString() {
        return "StashEnd";
    }
}
