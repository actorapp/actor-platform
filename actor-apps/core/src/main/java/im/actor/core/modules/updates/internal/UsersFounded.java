/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.core.modules.updates.internal;

import java.util.List;

import im.actor.core.api.ApiUser;
import im.actor.core.viewmodel.CommandCallback;
import im.actor.core.viewmodel.UserVM;

public class UsersFounded extends InternalUpdate {
    private List<ApiUser> users;
    private CommandCallback<UserVM[]> commandCallback;

    public UsersFounded(List<ApiUser> users, CommandCallback<UserVM[]> commandCallback) {
        this.users = users;
        this.commandCallback = commandCallback;
    }

    public List<ApiUser> getUsers() {
        return users;
    }

    public CommandCallback<UserVM[]> getCommandCallback() {
        return commandCallback;
    }
}
