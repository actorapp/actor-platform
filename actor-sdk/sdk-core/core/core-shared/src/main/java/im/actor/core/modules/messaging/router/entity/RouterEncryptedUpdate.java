package im.actor.core.modules.messaging.router.entity;

import im.actor.core.api.ApiEncryptedContent;
import im.actor.runtime.actors.ask.AskMessage;
import im.actor.runtime.actors.messages.Void;

public class RouterEncryptedUpdate implements AskMessage<Void> {

    private int senderId;
    private long date;
    private ApiEncryptedContent update;

    public RouterEncryptedUpdate(int senderId, long date, ApiEncryptedContent update) {
        this.senderId = senderId;
        this.date = date;
        this.update = update;
    }

    public int getSenderId() {
        return senderId;
    }

    public long getDate() {
        return date;
    }

    public ApiEncryptedContent getUpdate() {
        return update;
    }
}
