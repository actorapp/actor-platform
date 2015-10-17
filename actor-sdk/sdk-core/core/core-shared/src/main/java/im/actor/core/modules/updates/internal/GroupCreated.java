/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.core.modules.updates.internal;

import java.util.List;

import im.actor.core.api.ApiGroup;
import im.actor.core.api.ApiUser;
import im.actor.core.viewmodel.CommandCallback;

public class GroupCreated extends InternalUpdate {
    private ApiGroup group;

    private List<ApiUser> users;

    private CommandCallback<Integer> callback;
    public GroupCreated(ApiGroup group, List<ApiUser> users,CommandCallback<Integer> callback) {
        this.group = group;
        this.users = users;
        this.callback = callback;
    }

    public ApiGroup getGroup() {
        return group;
    }

    public List<ApiUser> getUsers() {
        return users;
    }

    public CommandCallback<Integer> getCallback() {
        return callback;
    }
}
