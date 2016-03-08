package im.actor.allmessages;

import java.util.ArrayList;

import im.actor.core.entity.Message;
import im.actor.core.entity.Peer;
import im.actor.core.modules.internal.messages.ConversationActor;
import im.actor.runtime.actors.ActorRef;

public class MessagesHandler implements ConversationActor.ConversationActorDelegate{

    Peer peer;
    ActorRef handler;


    public MessagesHandler(Peer peer, ActorRef handler) {
        this.peer = peer;
        this.handler = handler;
    }

    @Override
    public void onIncoming(ArrayList<Message> msgs) {
        handler.send(new OverHandlerActor.Incoming(peer, msgs));
    }

    @Override
    public void onIncoming(Message msg) {
        handler.send(new OverHandlerActor.Incoming(peer, msg));

    }

    @Override
    public void onUpdate(Message msg) {
        handler.send(new OverHandlerActor.Update(peer, msg));

    }

    @Override
    public void onDelete(long[] rids) {
        handler.send(new OverHandlerActor.Delete(peer, rids));

    }
}
