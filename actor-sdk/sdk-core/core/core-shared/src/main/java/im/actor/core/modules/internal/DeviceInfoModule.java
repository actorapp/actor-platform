package im.actor.core.modules.internal;

import im.actor.core.modules.AbsModule;
import im.actor.core.modules.ModuleContext;
import im.actor.core.modules.internal.device.DeviceInfoActor;
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
        actorRef = system().actorOf(Props.create(new ActorCreator() {
            @Override
            public DeviceInfoActor create() {
                return new DeviceInfoActor(context());
            }
        }), "device_info/notifier");
    }
}
