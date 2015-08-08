/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.core.modules.updates.internal;

import java.util.List;

import im.actor.core.api.User;
import im.actor.core.viewmodel.CommandCallback;
import im.actor.core.viewmodel.UserVM;

public class UsersFounded extends InternalUpdate {
    private List<User> users;
    private CommandCallback<UserVM[]> commandCallback;

    public UsersFounded(List<User> users, CommandCallback<UserVM[]> commandCallback) {
        this.users = users;
        this.commandCallback = commandCallback;
    }

    public List<User> getUsers() {
        return users;
    }

    public CommandCallback<UserVM[]> getCommandCallback() {
        return commandCallback;
    }
}
