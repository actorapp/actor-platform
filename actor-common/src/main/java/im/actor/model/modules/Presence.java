package im.actor.model.modules;

import im.actor.model.Messenger;
import im.actor.model.droidkit.actors.ActorCreator;
import im.actor.model.droidkit.actors.ActorRef;
import im.actor.model.droidkit.actors.Props;
import im.actor.model.entity.Peer;
import im.actor.model.modules.presence.OwnPresenceActor;
import im.actor.model.modules.presence.PresenceActor;

import static im.actor.model.droidkit.actors.ActorSystem.system;

/**
 * Created by ex3ndr on 15.02.15.
 */
public class Presence {
    private Messenger messenger;
    private ActorRef myPresence;
    private ActorRef presence;

    public Presence(final Messenger messenger) {
        this.messenger = messenger;
        this.myPresence = system().actorOf(Props.create(OwnPresenceActor.class, new ActorCreator<OwnPresenceActor>() {
            @Override
            public OwnPresenceActor create() {
                return new OwnPresenceActor(messenger);
            }
        }), "actor/presence/own");
        presence = PresenceActor.get(messenger);
    }

    public void run() {
        myPresence.send(new OwnPresenceActor.OnAppVisible());
    }

    public void onAppVisible() {
        myPresence.send(new OwnPresenceActor.OnAppVisible());
    }

    public void onAppHidden() {
        myPresence.send(new OwnPresenceActor.OnAppHidden());
    }

    public void onConversationOpen(Peer peer) {
        presence.send(new PresenceActor.Subscribe(peer));
    }

    public void onConversationClosed(Peer peer) {

    }

    public void onNewSessionCreated() {
        presence.send(new PresenceActor.SessionCreated());
    }
}