package im.actor.runtime.actors.tools;

import im.actor.runtime.actors.Actor;
import im.actor.runtime.actors.ActorRef;
import im.actor.runtime.actors.ActorTime;

public class BounceFilterActor extends Actor {

    private static final long BOUNCE_DELAY = 300;

    private long lastMessage = 0;
    private Message message;

    private void onMessage(Message message) {
        long time = ActorTime.currentTime();
        long delta = time - lastMessage;

        if (delta > BOUNCE_DELAY) {
            lastMessage = time;
            if (this.message == null || isOverride(this.message, message)) {
                // Send message
                message.actorRef.send(message.object);
            } else {
                // Send old message
                this.message.actorRef.send(this.message.object);
            }
            this.message = null;
        } else {
            // Too early
            if (this.message == null || isOverride(this.message, message)) {
                this.message = message;
                self().sendOnce(new Flush(), BOUNCE_DELAY - delta);
            }
        }
    }

    private void onFlush() {
        if (this.message != null) {
            this.message.actorRef.send(this.message.object);
            this.message = null;
            lastMessage = ActorTime.currentTime();
        }
    }

    protected boolean isOverride(Message current, Message next) {
        return true;
    }

    @Override
    public void onReceive(Object message) {
        if (message instanceof Message) {
            onMessage((Message) message);
        } else if (message instanceof Flush) {
            onFlush();
        } else {
            drop(message);
        }
    }

    public static class Message {

        private Object object;
        private ActorRef actorRef;

        public Message(Object object, ActorRef actorRef) {
            this.object = object;
            this.actorRef = actorRef;
        }

        public Object getObject() {
            return object;
        }

        public ActorRef getActorRef() {
            return actorRef;
        }
    }

    private static class Flush {

    }
}
