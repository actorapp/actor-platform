package im.actor.core.modules.messaging.dialogs.entity;

import java.util.List;

import im.actor.core.modules.messaging.history.entity.DialogHistory;
import im.actor.runtime.actors.ask.AskMessage;
import im.actor.runtime.actors.messages.Void;

public class HistoryLoaded implements AskMessage<Void> {
    
    private List<DialogHistory> history;

    public HistoryLoaded(List<DialogHistory> history) {
        this.history = history;
    }

    public List<DialogHistory> getHistory() {
        return history;
    }
}
