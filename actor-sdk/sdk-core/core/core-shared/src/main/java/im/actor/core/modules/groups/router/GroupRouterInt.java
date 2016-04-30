package im.actor.core.modules.groups.router;

import java.util.List;

import im.actor.core.api.ApiGroup;
import im.actor.core.api.ApiGroupOutPeer;
import im.actor.core.modules.ModuleContext;
import im.actor.core.modules.groups.router.entity.RouterApplyGroups;
import im.actor.core.modules.groups.router.entity.RouterFetchMissingGroups;
import im.actor.core.modules.groups.router.entity.RouterGroupUpdate;
import im.actor.core.network.parser.Update;
import im.actor.runtime.actors.ActorInterface;
import im.actor.runtime.actors.messages.Void;
import im.actor.runtime.promise.Promise;

import static im.actor.runtime.actors.ActorSystem.system;

public class GroupRouterInt extends ActorInterface {

    public GroupRouterInt(ModuleContext context) {
        setDest(system().actorOf("groups/router", () -> new GroupRouter(context)));
    }

    public Promise<Void> applyGroups(List<ApiGroup> groups) {
        return ask(new RouterApplyGroups(groups));
    }

    public Promise<List<ApiGroupOutPeer>> fetchPendingGroups(List<ApiGroupOutPeer> peers) {
        return ask(new RouterFetchMissingGroups(peers));
    }

    public Promise<Void> onUpdate(Update update) {
        return ask(new RouterGroupUpdate(update));
    }
}
