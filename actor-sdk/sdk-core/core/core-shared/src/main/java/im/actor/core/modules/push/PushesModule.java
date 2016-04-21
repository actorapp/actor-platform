/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.core.modules.push;

import im.actor.core.modules.AbsModule;
import im.actor.core.modules.ModuleContext;
import im.actor.core.modules.push.PushRegisterActor;
import im.actor.runtime.actors.ActorCreator;
import im.actor.runtime.actors.ActorRef;
import im.actor.runtime.actors.Props;

import static im.actor.runtime.actors.ActorSystem.system;

public class PushesModule extends AbsModule {

    private ActorRef pushRegisterActor;

    public PushesModule(ModuleContext modules) {
        super(modules);

        pushRegisterActor = system().actorOf("actor/push", () -> new PushRegisterActor(context()));
    }

    public void registerGooglePush(long projectId, String token) {
        pushRegisterActor.send(new PushRegisterActor.RegisterGooglePush(projectId, token));
    }

    public void registerApplePush(int apnsKey, String token) {
        pushRegisterActor.send(new PushRegisterActor.RegisterApplePush(apnsKey, token));
    }

    public void registerApplePushKit(int apnsKey, String token) {
        pushRegisterActor.send(new PushRegisterActor.RegisterAppleVoipPush(apnsKey, token));
    }

    public void registerActorPush(String endpoint) {
        pushRegisterActor.send(new PushRegisterActor.RegisterActorPush(endpoint));
    }
}
