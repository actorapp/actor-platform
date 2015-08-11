/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.core.modules.internal;

import im.actor.core.modules.AbsModule;
import im.actor.core.modules.ModuleContext;
import im.actor.core.modules.internal.push.PushRegisterActor;
import im.actor.runtime.actors.ActorCreator;
import im.actor.runtime.actors.ActorRef;
import im.actor.runtime.actors.Props;

import static im.actor.runtime.actors.ActorSystem.system;

public class PushesModule extends AbsModule {

    private ActorRef pushActor;

    public PushesModule(ModuleContext modules) {
        super(modules);

        pushActor = system().actorOf(Props.create(PushRegisterActor.class, new ActorCreator<PushRegisterActor>() {
            @Override
            public PushRegisterActor create() {
                return new PushRegisterActor(context());
            }
        }), "actor/push");
    }

    public void registerGooglePush(long projectId, String token) {
        pushActor.send(new PushRegisterActor.RegisterGooglePush(projectId, token));
    }

    public void registerApplePush(int apnsKey, String token) {
        pushActor.send(new PushRegisterActor.RegisterApplePush(apnsKey, token));
    }

    public void resetModule() {
        pushActor.send(new PushRegisterActor.ResendPush());
    }
}
