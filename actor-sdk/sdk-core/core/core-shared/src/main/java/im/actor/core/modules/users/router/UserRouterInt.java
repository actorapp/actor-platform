package im.actor.core.modules.users.router;

import java.util.List;

import im.actor.core.api.ApiUser;
import im.actor.core.api.ApiUserOutPeer;
import im.actor.core.modules.ModuleContext;
import im.actor.core.modules.users.router.entity.RouterApplyUsers;
import im.actor.core.modules.users.router.entity.RouterFetchMissingUsers;
import im.actor.core.modules.users.router.entity.RouterLoadFullUser;
import im.actor.core.modules.users.router.entity.RouterUserUpdate;
import im.actor.core.network.parser.Update;
import im.actor.runtime.actors.ActorInterface;
import im.actor.runtime.actors.messages.Void;
import im.actor.runtime.promise.Promise;

import static im.actor.runtime.actors.ActorSystem.system;

public class UserRouterInt extends ActorInterface {

    public UserRouterInt(ModuleContext context) {
        super(system().actorOf("users/router", () -> new UserRouter(context)));
    }

    public Promise<Void> applyUsers(List<ApiUser> users) {
        return ask(new RouterApplyUsers(users));
    }

    public Promise<List<ApiUserOutPeer>> fetchMissingUsers(List<ApiUserOutPeer> users) {
        return ask(new RouterFetchMissingUsers(users));
    }

    public Promise<Void> onUpdate(Update update) {
        return ask(new RouterUserUpdate(update));
    }

    public void onFullUserNeeded(int uid) {
        send(new RouterLoadFullUser(uid));
    }
}