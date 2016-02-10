package im.actor.core.modules.calls;

import im.actor.core.modules.ModuleContext;
import im.actor.core.modules.eventbus.EventBusActor;

public class CallSlaveActor extends EventBusActor {
    
    public CallSlaveActor(String busId, ModuleContext context) {
        super(busId, context);
    }
}
