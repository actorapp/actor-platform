package im.actor.runtime.actors;

public class StashedMessage {

    private Object message;
    private ActorRef sender;

    public StashedMessage(Object message, ActorRef sender) {
        this.message = message;
        this.sender = sender;
    }

    public Object getMessage() {
        return message;
    }

    public ActorRef getSender() {
        return sender;
    }
}
