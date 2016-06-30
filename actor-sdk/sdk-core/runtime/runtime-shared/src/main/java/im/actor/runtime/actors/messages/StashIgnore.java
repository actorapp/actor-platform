package im.actor.runtime.actors.messages;

public class StashIgnore {

    private Object message;

    public StashIgnore(Object message) {
        this.message = message;
    }

    public Object getMessage() {
        return message;
    }
}