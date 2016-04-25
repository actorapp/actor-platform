package im.actor.core.modules.sequence.internal;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

import im.actor.core.api.ApiGroup;
import im.actor.core.api.ApiUser;
import im.actor.core.network.parser.Update;
import im.actor.runtime.actors.ask.AskMessage;
import im.actor.runtime.actors.messages.Void;

public class HandlerSeqUpdate implements AskMessage<Void> {

    @NotNull
    private Update update;
    @Nullable
    private List<ApiUser> users;
    @Nullable
    private
    List<ApiGroup> groups;

    public HandlerSeqUpdate(@NotNull Update update, @Nullable List<ApiUser> users, @Nullable List<ApiGroup> groups) {
        this.update = update;
        this.users = users;
        this.groups = groups;
    }

    @NotNull
    public Update getUpdate() {
        return update;
    }

    public
    @Nullable
    List<ApiUser> getUsers() {
        return users;
    }

    public
    @Nullable
    List<ApiGroup> getGroups() {
        return groups;
    }
}
