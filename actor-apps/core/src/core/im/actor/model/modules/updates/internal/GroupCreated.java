/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.model.modules.updates.internal;

import java.util.List;

import im.actor.model.api.Group;
import im.actor.model.api.User;
import im.actor.model.concurrency.CommandCallback;

public class GroupCreated extends InternalUpdate {
    private Group group;

    private List<User> users;

    private CommandCallback<Integer> callback;
    public GroupCreated(Group group, List<User> users,CommandCallback<Integer> callback) {
        this.group = group;
        this.users = users;
        this.callback = callback;
    }

    public Group getGroup() {
        return group;
    }

    public List<User> getUsers() {
        return users;
    }

    public CommandCallback<Integer> getCallback() {
        return callback;
    }
}
