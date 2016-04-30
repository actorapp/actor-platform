package im.actor.core.modules.groups.router.entity;

import im.actor.core.network.parser.Update;
import im.actor.runtime.actors.ask.AskMessage;
import im.actor.runtime.actors.messages.Void;

public class RouterGroupUpdate implements AskMessage<Void> {

    private Update update;

    public RouterGroupUpdate(Update update) {
        this.update = update;
    }

    public Update getUpdate() {
        return update;
    }
}
