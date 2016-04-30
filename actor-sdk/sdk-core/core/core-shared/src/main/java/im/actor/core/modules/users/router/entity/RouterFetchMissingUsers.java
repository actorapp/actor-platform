package im.actor.core.modules.users.router.entity;

import java.util.List;

import im.actor.core.api.ApiUserOutPeer;
import im.actor.runtime.actors.ask.AskMessage;

public class RouterFetchMissingUsers implements AskMessage<List<ApiUserOutPeer>> {

    private List<ApiUserOutPeer> sourcePeers;

    public RouterFetchMissingUsers(List<ApiUserOutPeer> sourcePeers) {
        this.sourcePeers = sourcePeers;
    }

    public List<ApiUserOutPeer> getSourcePeers() {
        return sourcePeers;
    }
}
