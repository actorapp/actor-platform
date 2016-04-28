package im.actor.core.modules.users.router.entity;

import im.actor.core.network.parser.Update;
import im.actor.runtime.actors.ask.AskMessage;
import im.actor.runtime.actors.messages.Void;

public class RouterUserUpdate implements AskMessage<Void> {

    private Update update;

    public RouterUserUpdate(Update update) {
        this.update = update;
    }

    public Update getUpdate() {
        return update;
    }
}
