package im.actor.core.modules.messaging.dialogs.entity;

import im.actor.core.entity.User;
import im.actor.runtime.actors.ask.AskMessage;
import im.actor.runtime.actors.messages.Void;

public class UserChanged implements AskMessage<Void> {

    private User user;

    public UserChanged(User user) {
        this.user = user;
    }

    public User getUser() {
        return user;
    }
}
