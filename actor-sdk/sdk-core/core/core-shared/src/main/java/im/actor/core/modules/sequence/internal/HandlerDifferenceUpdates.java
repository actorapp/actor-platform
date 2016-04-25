package im.actor.core.modules.sequence.internal;

import org.jetbrains.annotations.NotNull;

import java.util.List;

import im.actor.core.api.ApiGroup;
import im.actor.core.api.ApiGroupOutPeer;
import im.actor.core.api.ApiMessageContainer;
import im.actor.core.api.ApiUser;
import im.actor.core.api.ApiUserOutPeer;
import im.actor.core.network.parser.Update;
import im.actor.runtime.actors.ask.AskMessage;
import im.actor.runtime.actors.messages.Void;

public class HandlerDifferenceUpdates implements AskMessage<Void> {

    @NotNull
    private List<ApiUser> users;
    @NotNull
    private List<ApiGroup> groups;
    @NotNull
    private List<ApiUserOutPeer> userOutPeers;
    @NotNull
    private List<ApiGroupOutPeer> groupOutPeers;
    @NotNull
    private List<Update> updates;

    public HandlerDifferenceUpdates(@NotNull List<ApiUser> users,
                                    @NotNull List<ApiGroup> groups,
                                    @NotNull List<ApiUserOutPeer> userOutPeers,
                                    @NotNull List<ApiGroupOutPeer> groupOutPeers,
                                    @NotNull List<Update> updates) {
        this.users = users;
        this.groups = groups;
        this.userOutPeers = userOutPeers;
        this.groupOutPeers = groupOutPeers;
        this.updates = updates;
    }

    @NotNull
    public List<ApiUser> getUsers() {
        return users;
    }

    @NotNull
    public List<ApiGroup> getGroups() {
        return groups;
    }

    @NotNull
    public List<ApiUserOutPeer> getUserOutPeers() {
        return userOutPeers;
    }

    @NotNull
    public List<ApiGroupOutPeer> getGroupOutPeers() {
        return groupOutPeers;
    }

    @NotNull
    public List<Update> getUpdates() {
        return updates;
    }
}
