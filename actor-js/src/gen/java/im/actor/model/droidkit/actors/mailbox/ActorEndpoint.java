package im.actor.model.droidkit.actors.mailbox;

import im.actor.model.droidkit.actors.ActorScope;

/**
 * Created by ex3ndr on 14.09.14.
 */
public class ActorEndpoint {
    private String path;
    private Mailbox mailbox;
    private ActorScope scope;
    private boolean isDisconnected;

    public ActorEndpoint(String path) {
        this.path = path;
        isDisconnected = false;
    }

    public String getPath() {
        return path;
    }

    public Mailbox getMailbox() {
        return mailbox;
    }

    public ActorScope getScope() {
        return scope;
    }

    public boolean isDisconnected() {
        return isDisconnected;
    }

    public void connect(Mailbox mailbox, ActorScope scope) {
        isDisconnected = false;
        this.mailbox = mailbox;
        this.scope = scope;
    }
}
