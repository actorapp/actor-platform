package im.actor.core.modules.groups.router;

import im.actor.core.modules.ModuleContext;
import im.actor.core.modules.groups.router.entity.GroupUpdate;
import im.actor.core.network.parser.Update;
import im.actor.runtime.actors.ActorInterface;
import im.actor.runtime.actors.messages.Void;
import im.actor.runtime.promise.Promise;

import static im.actor.runtime.actors.ActorSystem.system;

public class GroupRouterInt extends ActorInterface {

    public GroupRouterInt(ModuleContext context) {
        setDest(system().actorOf("groups/router", () -> new GroupRouter(context)));
    }

    public Promise<Void> onUpdate(Update update) {
        return ask(new GroupUpdate(update));
    }
}
