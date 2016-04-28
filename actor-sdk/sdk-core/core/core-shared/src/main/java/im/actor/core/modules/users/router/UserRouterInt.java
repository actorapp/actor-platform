package im.actor.core.modules.users.router;

import java.util.List;

import im.actor.core.api.ApiAvatar;
import im.actor.core.api.ApiUser;
import im.actor.core.api.ApiUserOutPeer;
import im.actor.core.modules.ModuleContext;
import im.actor.core.modules.users.router.entity.RouterAboutChanged;
import im.actor.core.modules.users.router.entity.RouterApplyUsers;
import im.actor.core.modules.users.router.entity.RouterAvatarChanged;
import im.actor.core.modules.users.router.entity.RouterFetchMissingUsers;
import im.actor.core.modules.users.router.entity.RouterLoadFullUser;
import im.actor.core.modules.users.router.entity.RouterLocalNameChanged;
import im.actor.core.modules.users.router.entity.RouterNameChanged;
import im.actor.core.modules.users.router.entity.RouterNicknameChanged;
import im.actor.core.modules.users.router.entity.RouterUserRegistered;
import im.actor.runtime.actors.ActorInterface;
import im.actor.runtime.actors.messages.Void;
import im.actor.runtime.promise.Promise;

import static im.actor.runtime.actors.ActorSystem.system;

public class UserRouterInt extends ActorInterface {

    public UserRouterInt(ModuleContext context) {
        super(system().actorOf("users/router", () -> new UserRouter(context)));
    }

    // Updates

    public Promise<Void> onUserNameChanged(int uid, String name) {
        return ask(new RouterNameChanged(uid, name));
    }

    public Promise<Void> onUserLocalNameChanged(int uid, String localName) {
        return ask(new RouterLocalNameChanged(uid, localName));
    }

    public Promise<Void> onUserNicknameChanged(int uid, String userName) {
        return ask(new RouterNicknameChanged(uid, userName));
    }

    public Promise<Void> onUserAvatarChanged(int uid, ApiAvatar avatar) {
        return ask(new RouterAvatarChanged(uid, avatar));
    }

    public Promise<Void> onUserAboutChanged(int uid, String about) {
        return ask(new RouterAboutChanged(uid, about));
    }

    public Promise<Void> onUserRegistered(int uid, long rid, long date) {
        return ask(new RouterUserRegistered(rid, uid, date));
    }

    // Entities

    public void onFullUserNeeded(int uid) {
        send(new RouterLoadFullUser(uid));
    }

    public Promise<List<ApiUserOutPeer>> fetchMissingUsers(List<ApiUserOutPeer> users) {
        return ask(new RouterFetchMissingUsers(users));
    }

    public Promise<Void> applyUsers(List<ApiUser> users) {
        return ask(new RouterApplyUsers(users));
    }
}