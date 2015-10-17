/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.core.modules.internal;

import im.actor.core.modules.AbsModule;
import im.actor.core.modules.Modules;
import im.actor.core.modules.internal.presence.OwnPresenceActor;
import im.actor.core.modules.internal.presence.PresenceActor;
import im.actor.runtime.actors.ActorCreator;
import im.actor.runtime.actors.Props;

import static im.actor.runtime.actors.ActorSystem.system;

public class PresenceModule extends AbsModule {

    public PresenceModule(final Modules modules) {
        super(modules);

        // Creating own presence actor
        system().actorOf(Props.create(OwnPresenceActor.class, new ActorCreator<OwnPresenceActor>() {
            @Override
            public OwnPresenceActor create() {
                return new OwnPresenceActor(modules);
            }
        }), "actor/presence/own");

        // Creating users and groups presence actor
        PresenceActor.create(modules);
    }
}