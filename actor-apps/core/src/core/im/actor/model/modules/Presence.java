/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.model.modules;

import im.actor.model.droidkit.actors.ActorCreator;
import im.actor.model.droidkit.actors.ActorRef;
import im.actor.model.droidkit.actors.Props;
import im.actor.model.entity.Peer;
import im.actor.model.modules.presence.OwnPresenceActor;
import im.actor.model.modules.presence.PresenceActor;

import static im.actor.model.droidkit.actors.ActorSystem.system;

public class Presence extends BaseModule {
    private ActorRef myPresence;
    private ActorRef presence;

    public Presence(final Modules modules) {
        super(modules);
        this.myPresence = system().actorOf(Props.create(OwnPresenceActor.class, new ActorCreator<OwnPresenceActor>() {
            @Override
            public OwnPresenceActor create() {
                return new OwnPresenceActor(modules);
            }
        }), "actor/presence/own");
        presence = PresenceActor.get(modules);
    }

    public void run() {
        // myPresence.send(new OwnPresenceActor.OnAppVisible());
    }

    public void onAppVisible() {
        myPresence.send(new OwnPresenceActor.OnAppVisible());
    }

    public void onAppHidden() {
        myPresence.send(new OwnPresenceActor.OnAppHidden());
    }

    public void subscribe(Peer peer) {
        presence.send(new PresenceActor.Subscribe(peer));
    }

    public void onNewSessionCreated() {
        presence.send(new PresenceActor.SessionCreated());
    }

    public void resetModule() {
        // TODO: Implement
    }
}