package im.actor.core.modules.messaging.router.entity;

import java.util.List;

import im.actor.core.modules.messaging.history.entity.DialogHistory;
import im.actor.runtime.actors.ask.AskMessage;
import im.actor.runtime.actors.messages.Void;

public class RouterApplyDialogsHistory implements AskMessage<Void>, RouterMessageOnlyActive {

    private List<DialogHistory> dialogs;

    public RouterApplyDialogsHistory(List<DialogHistory> dialogs) {
        this.dialogs = dialogs;
    }

    public List<DialogHistory> getDialogs() {
        return dialogs;
    }

}
