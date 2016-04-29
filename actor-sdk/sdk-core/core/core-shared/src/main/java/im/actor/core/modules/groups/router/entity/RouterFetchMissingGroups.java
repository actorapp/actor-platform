package im.actor.core.modules.groups.router.entity;

import java.util.List;

import im.actor.core.api.ApiGroupOutPeer;
import im.actor.runtime.actors.ask.AskMessage;

public class RouterFetchMissingGroups implements AskMessage<List<ApiGroupOutPeer>> {

    private List<ApiGroupOutPeer> groups;

    public RouterFetchMissingGroups(List<ApiGroupOutPeer> groups) {
        this.groups = groups;
    }

    public List<ApiGroupOutPeer> getGroups() {
        return groups;
    }
}
