package im.actor.core.modules.eventbus;

import im.actor.core.api.updates.UpdateEventBusDeviceConnected;
import im.actor.core.api.updates.UpdateEventBusDeviceDisconnected;
import im.actor.core.api.updates.UpdateEventBusDisposed;
import im.actor.core.api.updates.UpdateEventBusMessage;
import im.actor.core.modules.ModuleContext;
import im.actor.core.modules.sequence.processor.WeakProcessor;
import im.actor.core.network.parser.Update;

public class EventBusProcessor implements WeakProcessor {

    private ModuleContext context;

    public EventBusProcessor(ModuleContext context) {
        this.context = context;
    }

    @Override
    public boolean process(Update update, long date) {
        if (update instanceof UpdateEventBusMessage ||
                update instanceof UpdateEventBusDeviceConnected ||
                update instanceof UpdateEventBusDeviceDisconnected ||
                update instanceof UpdateEventBusDisposed) {
            context.getEventBus().onEventBusUpdate(update);
            return true;
        }

        return false;
    }
}