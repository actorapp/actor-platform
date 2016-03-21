/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.core.modules.internal;

import im.actor.core.modules.AbsModule;
import im.actor.core.modules.ModuleContext;
import im.actor.core.modules.internal.stickers.StickersActor;
import im.actor.core.viewmodel.StickersVM;
import im.actor.runtime.actors.ActorCreator;
import im.actor.runtime.actors.ActorRef;
import im.actor.runtime.actors.Props;

import static im.actor.runtime.actors.ActorSystem.system;

public class StickersModule extends AbsModule {

    private ActorRef stickersActor;
    private StickersVM stickersVM;

    public StickersModule(ModuleContext context) {
        super(context);
    }

    public void run() {
        this.stickersVM = new StickersVM();
        this.stickersActor = system().actorOf(Props.create(new ActorCreator() {
            @Override
            public StickersActor create() {
                return new StickersActor(context());
            }
        }), "actor/stickers");
    }

    public ActorRef getStickersActor() {
        return stickersActor;
    }

    public StickersVM getStickersVM() {
        return stickersVM;
    }
}