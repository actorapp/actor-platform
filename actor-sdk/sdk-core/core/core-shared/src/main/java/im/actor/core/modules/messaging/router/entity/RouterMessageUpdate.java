package im.actor.core.modules.messaging.router.entity;

import im.actor.core.network.parser.Update;
import im.actor.runtime.actors.ask.AskMessage;
import im.actor.runtime.actors.messages.Void;

public class RouterMessageUpdate implements AskMessage<Void>, RouterMessageOnlyActive {

    private Update update;

    public RouterMessageUpdate(Update update) {
        this.update = update;
    }

    public Update getUpdate() {
        return update;
    }
}
