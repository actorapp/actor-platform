package im.actor.core.modules.messaging.dialogs.entity;

import im.actor.core.entity.Peer;
import im.actor.runtime.actors.ask.AskMessage;
import im.actor.runtime.actors.messages.Void;

public class CounterChanged implements AskMessage<Void> {

    private Peer peer;
    private int counter;

    public CounterChanged(Peer peer, int counter) {
        this.peer = peer;
        this.counter = counter;
    }

    public Peer getPeer() {
        return peer;
    }

    public int getCounter() {
        return counter;
    }
}
