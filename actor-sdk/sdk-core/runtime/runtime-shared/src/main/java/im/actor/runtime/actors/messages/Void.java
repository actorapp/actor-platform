package im.actor.runtime.actors.messages;

public class Void {

    public static final Void INSTANCE = new Void();

    private Void() {
    }

    @Override
    public String toString() {
        return "Void";
    }
}
