package im.actor.model.modules;

import com.droidkit.actors.ActorCreator;
import com.droidkit.actors.ActorRef;
import com.droidkit.actors.Props;
import im.actor.model.Messenger;
import im.actor.model.State;
import im.actor.model.api.MessageContent;
import im.actor.model.api.rpc.RequestSendMessage;
import im.actor.model.api.rpc.RequestSignIn;
import im.actor.model.api.rpc.ResponseAuth;
import im.actor.model.concurrency.Command;
import im.actor.model.concurrency.CommandCallback;
import im.actor.model.entity.Dialog;
import im.actor.model.entity.Message;
import im.actor.model.entity.Peer;
import im.actor.model.modules.messages.ConversationActor;
import im.actor.model.modules.messages.DialogsActor;
import im.actor.model.modules.messages.DialogsHistoryActor;
import im.actor.model.mvvm.ListEngine;
import im.actor.model.network.RpcCallback;
import im.actor.model.network.RpcException;
import im.actor.model.util.RandomUtils;

import java.util.HashMap;

import static com.droidkit.actors.ActorSystem.system;

/**
 * Created by ex3ndr on 09.02.15.
 */
public class Messages {
    private Messenger messenger;
    private ListEngine<Dialog> dialogs;
    private ActorRef dialogsActor;
    private ActorRef dialogsHistoryActor;

    private final HashMap<Peer, ListEngine<Message>> conversationEngines = new HashMap<Peer, ListEngine<Message>>();
    private final HashMap<Peer, ActorRef> conversationActors = new HashMap<Peer, ActorRef>();

    public Messages(final Messenger messenger) {
        this.messenger = messenger;
        this.dialogs = messenger.getConfiguration().getEnginesFactory().createDialogsEngine();
    }

    public void run() {
        this.dialogsActor = system().actorOf(Props.create(DialogsActor.class, new ActorCreator<DialogsActor>() {
            @Override
            public DialogsActor create() {
                return new DialogsActor(messenger);
            }
        }), "actor/dialogs");
        this.dialogsHistoryActor = system().actorOf(Props.create(DialogsHistoryActor.class, new ActorCreator<DialogsHistoryActor>() {
            @Override
            public DialogsHistoryActor create() {
                return new DialogsHistoryActor(messenger);
            }
        }), "actor/dialogs/history");
    }

    public ActorRef getConversationActor(final Peer peer) {
        synchronized (conversationActors) {
            if (!conversationActors.containsKey(peer)) {
                conversationActors.put(peer, system().actorOf(Props.create(ConversationActor.class,
                        new ActorCreator<ConversationActor>() {
                            @Override
                            public ConversationActor create() {
                                return new ConversationActor(peer, messenger);
                            }
                        }), "actor/conv_" + peer.getPeerType() + "_" + peer.getPeerId()));
            }
            return conversationActors.get(peer);
        }
    }

    public ListEngine<Message> getConversationEngine(Peer peer) {
        synchronized (conversationEngines) {
            if (!conversationEngines.containsKey(peer)) {
                conversationEngines.put(peer, messenger.getConfiguration().getEnginesFactory().createMessagesEngine(peer));
            }
            return conversationEngines.get(peer);
        }
    }

    public ActorRef getDialogsActor() {
        return dialogsActor;
    }

    public ActorRef getDialogsHistoryActor() {
        return dialogsHistoryActor;
    }

    public ListEngine<Dialog> getDialogsEngine() {
        return dialogs;
    }

    public void sendMessages(final Peer peer, final MessageContent message) {

    }
}
