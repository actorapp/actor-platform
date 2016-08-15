package im.actor.core.modules.raw;

import im.actor.core.Configuration;
import im.actor.core.RawUpdatesHandler;
import im.actor.core.api.updates.UpdateRawUpdate;
import im.actor.core.modules.AbsModule;
import im.actor.core.modules.ModuleContext;
import im.actor.core.modules.sequence.processor.SequenceProcessor;
import im.actor.core.modules.sequence.processor.UpdateProcessor;
import im.actor.core.network.parser.Update;
import im.actor.runtime.Log;
import im.actor.runtime.actors.ActorRef;
import im.actor.runtime.actors.ActorSystem;
import im.actor.runtime.actors.Props;
import im.actor.runtime.actors.messages.*;
import im.actor.runtime.actors.messages.Void;
import im.actor.runtime.promise.Promise;
import im.actor.sdk.core.audio.AudioPlayerActor;

public class RawProcessor extends AbsModule implements SequenceProcessor {
    private static final String TAG = "RawProcessor";
    private final ActorRef rawUpdatesHandlerActor;

    public RawProcessor(ModuleContext context) {
        super(context);
        RawUpdatesHandler rawUpdatesHandler = context().getConfiguration().getRawUpdatesHandlerProvider().getRawUpdatesHandler();
        if (rawUpdatesHandler != null) {
            rawUpdatesHandlerActor = ActorSystem.system().actorOf(Props.create(() -> rawUpdatesHandler), "actor/raw_updates");
        } else {
            rawUpdatesHandlerActor = ActorSystem.system().actorOf(Props.create(() -> new RawUpdatesHandler() {
                @Override
                protected void onRawUpdate(UpdateRawUpdate update) {
                    Log.d(TAG, "update: " + update.toString());
                }
            }), "actor/raw_updates");

        }
    }

    @Override
    public Promise<Void> process(Update update) {
        if (update instanceof UpdateRawUpdate) {
            rawUpdatesHandlerActor.send(update);
            return Promise.success(null);
        }
        return null;
    }
}
