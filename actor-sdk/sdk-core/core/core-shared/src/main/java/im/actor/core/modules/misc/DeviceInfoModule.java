package im.actor.core.modules.misc;

import im.actor.core.modules.AbsModule;
import im.actor.core.modules.ModuleContext;
import im.actor.core.modules.misc.DeviceInfoActor;
import im.actor.runtime.actors.ActorCreator;
import im.actor.runtime.actors.ActorRef;
import im.actor.runtime.actors.Props;

import static im.actor.runtime.actors.ActorSystem.system;

public class DeviceInfoModule extends AbsModule {

    private ActorRef actorRef;

    public DeviceInfoModule(ModuleContext context) {
        super(context);
    }

    public void run() {
        actorRef = system().actorOf("device_info/notifier", () -> new DeviceInfoActor(context()));
    }
}
