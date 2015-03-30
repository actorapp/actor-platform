package im.actor.model.modules.updates.internal;

import im.actor.model.api.Group;
import im.actor.model.concurrency.CommandCallback;

/**
 * Created by ex3ndr on 27.02.15.
 */
public class GroupCreated extends InternalUpdate {
    private Group group;
    private CommandCallback<Integer> callback;

    public GroupCreated(Group group, CommandCallback<Integer> callback) {
        this.group = group;
        this.callback = callback;
    }

    public Group getGroup() {
        return group;
    }

    public CommandCallback<Integer> getCallback() {
        return callback;
    }
}
