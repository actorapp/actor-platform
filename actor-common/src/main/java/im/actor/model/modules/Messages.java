package im.actor.model.modules;

import im.actor.model.droidkit.actors.ActorCreator;
import im.actor.model.droidkit.actors.ActorRef;
import im.actor.model.droidkit.actors.Props;
import im.actor.model.entity.Dialog;
import im.actor.model.entity.Message;
import im.actor.model.entity.Peer;
import im.actor.model.entity.ReadState;
import im.actor.model.modules.messages.ConversationActor;
import im.actor.model.modules.messages.DialogsActor;
import im.actor.model.modules.messages.DialogsHistoryActor;
import im.actor.model.modules.messages.OwnReadActor;
import im.actor.model.modules.messages.PlainReaderActor;
import im.actor.model.modules.messages.PlainReceiverActor;
import im.actor.model.modules.messages.SenderActor;
import im.actor.model.storage.KeyValueEngine;
import im.actor.model.storage.ListEngine;

import java.util.HashMap;

import static im.actor.model.droidkit.actors.ActorSystem.system;

/**
 * Created by ex3ndr on 09.02.15.
 */
public class Messages extends BaseModule {

    private ListEngine<Dialog> dialogs;
    private ActorRef dialogsActor;
    private ActorRef dialogsHistoryActor;
    private ActorRef ownReadActor;
    private ActorRef plainReadActor;
    private ActorRef plainReceiverActor;
    private ActorRef sendMessageActor;

    private final HashMap<Peer, ListEngine<Message>> conversationEngines = new HashMap<Peer, ListEngine<Message>>();
    private final HashMap<Peer, ActorRef> conversationActors = new HashMap<Peer, ActorRef>();
    private KeyValueEngine<ReadState> readStates;

    public Messages(final Modules messenger) {
        super(messenger);
        this.dialogs = messenger.getConfiguration().getStorage().createDialogsEngine();
        this.readStates = messenger.getConfiguration().getStorage().createReadStateEngine();
    }

    public void run() {
        this.dialogsActor = system().actorOf(Props.create(DialogsActor.class, new ActorCreator<DialogsActor>() {
            @Override
            public DialogsActor create() {
                return new DialogsActor(modules());
            }
        }), "actor/dialogs");
        this.dialogsHistoryActor = system().actorOf(Props.create(DialogsHistoryActor.class, new ActorCreator<DialogsHistoryActor>() {
            @Override
            public DialogsHistoryActor create() {
                return new DialogsHistoryActor(modules());
            }
        }), "actor/dialogs/history");
        this.ownReadActor = system().actorOf(Props.create(OwnReadActor.class, new ActorCreator<OwnReadActor>() {
            @Override
            public OwnReadActor create() {
                return new OwnReadActor(modules());
            }
        }), "actor/read/own");
        this.plainReadActor = system().actorOf(Props.create(PlainReaderActor.class, new ActorCreator<PlainReaderActor>() {
            @Override
            public PlainReaderActor create() {
                return new PlainReaderActor(modules());
            }
        }), "actor/plain/read");
        this.plainReceiverActor = system().actorOf(Props.create(PlainReceiverActor.class, new ActorCreator<PlainReceiverActor>() {
            @Override
            public PlainReceiverActor create() {
                return new PlainReceiverActor(modules());
            }
        }), "actor/plain/receive");
        this.sendMessageActor = system().actorOf(Props.create(SenderActor.class, new ActorCreator<SenderActor>() {
            @Override
            public SenderActor create() {
                return new SenderActor(modules());
            }
        }), "actor/sender/small");
    }

    public ActorRef getSendMessageActor() {
        return sendMessageActor;
    }

    public ActorRef getPlainReadActor() {
        return plainReadActor;
    }

    public ActorRef getPlainReceiverActor() {
        return plainReceiverActor;
    }

    public ActorRef getOwnReadActor() {
        return ownReadActor;
    }

    public ActorRef getConversationActor(final Peer peer) {
        synchronized (conversationActors) {
            if (!conversationActors.containsKey(peer)) {
                conversationActors.put(peer, system().actorOf(Props.create(ConversationActor.class,
                        new ActorCreator<ConversationActor>() {
                            @Override
                            public ConversationActor create() {
                                return new ConversationActor(peer, modules());
                            }
                        }), "actor/conv_" + peer.getPeerType() + "_" + peer.getPeerId()));
            }
            return conversationActors.get(peer);
        }
    }

    public ListEngine<Message> getConversationEngine(Peer peer) {
        synchronized (conversationEngines) {
            if (!conversationEngines.containsKey(peer)) {
                conversationEngines.put(peer, modules().getConfiguration().getStorage().createMessagesEngine(peer));
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

    public KeyValueEngine<ReadState> getReadStates() {
        return readStates;
    }

    public void sendMessage(final Peer peer, final String message) {
        sendMessageActor.send(new SenderActor.SendText(peer, message));
    }

    public void onInMessageShown(Peer peer, long rid, long sortDate, boolean isEncrypted) {
        ownReadActor.send(new OwnReadActor.MessageRead(peer, rid, sortDate, isEncrypted));
    }

    public void saveDraft(Peer peer, String draft) {
        preferences().putString("draft_" + peer.getUid(), draft.trim());
    }

    public String loadDraft(Peer peer) {
        String res = preferences().getString("draft_" + peer.getUid());
        if (res == null) {
            return "";
        } else {
            return res;
        }
    }
}