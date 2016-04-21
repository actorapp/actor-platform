/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.core.modules.stickers;

import im.actor.core.modules.AbsModule;
import im.actor.core.modules.ModuleContext;
import im.actor.core.modules.stickers.StickersActor;
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
        this.stickersActor = system().actorOf("actor/stickers", () -> new StickersActor(context()));
    }

    public ActorRef getStickersActor() {
        return stickersActor;
    }

    public StickersVM getStickersVM() {
        return stickersVM;
    }
}