package im.actor.core.modules.messaging.dialogs.entity;

import im.actor.core.entity.Group;
import im.actor.runtime.actors.ask.AskMessage;
import im.actor.runtime.actors.messages.Void;

public class GroupChanged implements AskMessage<Void> {

    private Group group;

    public GroupChanged(Group group) {
        this.group = group;
    }

    public Group getGroup() {
        return group;
    }
}
