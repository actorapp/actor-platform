package im.actor.core.modules.messaging.actors;

import im.actor.core.modules.ModuleContext;
import im.actor.core.modules.messaging.actors.entity.MessageShownEvent;
import im.actor.core.util.ModuleActor;

public class MessageShownActor extends ModuleActor {

    public MessageShownActor(ModuleContext context) {
        super(context);
    }

    @Override
    public void onReceive(Object message) {
        if (message instanceof MessageShownEvent) {
            MessageShownEvent shownEvent = (MessageShownEvent) message;
            context().getMessagesModule().getOwnReadActor().send(new OwnReadActor.MessageRead(shownEvent.getPeer(),
                    shownEvent.getSortDate()));
            context().getMessagesModule().getConversationActor(shownEvent.getPeer())
                    .send(new ConversationActor.MessageReadByMe(shownEvent.getSortDate()));
        } else {
            drop(message);
        }
    }
}
