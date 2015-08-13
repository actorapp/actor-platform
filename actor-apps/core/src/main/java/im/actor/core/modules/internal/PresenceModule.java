/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.core.modules.internal;

import im.actor.core.modules.AbsModule;
import im.actor.core.modules.Modules;
import im.actor.runtime.actors.ActorCreator;
import im.actor.runtime.actors.ActorRef;
import im.actor.runtime.actors.Props;
import im.actor.core.entity.PeerEntity;
import im.actor.core.modules.internal.presence.OwnPresenceActor;
import im.actor.core.modules.internal.presence.PresenceActor;

import static im.actor.runtime.actors.ActorSystem.system;

public class PresenceModule extends AbsModule {

    private ActorRef myPresence;
    private ActorRef presence;

    public PresenceModule(final Modules modules) {
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

    public void subscribe(PeerEntity peer) {
        presence.send(new PresenceActor.Subscribe(peer));
    }

    public void onNewSessionCreated() {
        presence.send(new PresenceActor.SessionCreated());
    }

    public void resetModule() {
        // TODO: Implement
    }
}