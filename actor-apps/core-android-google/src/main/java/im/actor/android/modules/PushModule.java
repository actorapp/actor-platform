package im.actor.android.modules;

import android.content.Context;

import im.actor.android.modules.push.PushActor;
import im.actor.model.droidkit.actors.ActorCreator;
import im.actor.model.droidkit.actors.ActorRef;
import im.actor.model.droidkit.actors.Props;
import im.actor.model.modules.BaseModule;
import im.actor.model.modules.Modules;

import static im.actor.model.droidkit.actors.ActorSystem.system;

/**
 * Created by ex3ndr on 02.04.15.
 */
public class PushModule extends BaseModule {
    private ActorRef pushActor;

    public PushModule(final Context context, final Modules modules) {
        super(modules);

        pushActor = system().actorOf(Props.create(PushActor.class, new ActorCreator<PushActor>() {
            @Override
            public PushActor create() {
                return new PushActor(context, modules);
            }
        }), "actor/android/push");
    }
}
