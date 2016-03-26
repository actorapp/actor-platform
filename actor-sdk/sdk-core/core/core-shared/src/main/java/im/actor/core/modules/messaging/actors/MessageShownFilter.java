package im.actor.core.modules.messaging.actors;

import im.actor.core.modules.messaging.actors.entity.MessageShownEvent;
import im.actor.runtime.actors.tools.BounceFilterActor;

public class MessageShownFilter extends BounceFilterActor {

    @Override
    protected boolean isOverride(Message current, Message next) {
        MessageShownEvent c = (MessageShownEvent) current.getObject();
        MessageShownEvent n = (MessageShownEvent) next.getObject();

        return c.getSortDate() < n.getSortDate();
    }
}
