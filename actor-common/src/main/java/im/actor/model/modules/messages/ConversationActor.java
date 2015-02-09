package im.actor.model.modules.messages;

import com.droidkit.actors.Actor;
import com.droidkit.actors.ActorRef;
import im.actor.model.Messenger;
import im.actor.model.entity.Message;
import im.actor.model.entity.Peer;
import im.actor.model.mvvm.ListEngine;

/**
 * Created by ex3ndr on 09.02.15.
 */
public class ConversationActor extends Actor {

    private Messenger messenger;
    private Peer peer;
    private ListEngine<Message> messages;
    private ActorRef dialogsActor;

    public ConversationActor(Peer peer, Messenger messenger) {
        this.peer = peer;
        this.messenger = messenger;
    }

    @Override
    public void preStart() {
        messages = messenger.getMessages(peer);
        dialogsActor = messenger.getMessagesModule().getDialogsActor();
    }

    @Override
    public void onReceive(Object message) {
        if (message instanceof Message) {
            onInMessage((Message) message);
        }
    }

    private void onInMessage(Message message) {
        if (messages.getValue(message.getListId()) != null) {
            return;
        }

        // Write message
        messages.addOrUpdateItem(message);
        // Updating dialogs
        dialogsActor.send(new DialogsActor.InMessage(peer, message));
    }
}