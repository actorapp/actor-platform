package im.actor.core.modules.raw;


import im.actor.core.RawUpdatesHandler;
import im.actor.core.api.updates.UpdateRawUpdate;
import im.actor.core.modules.AbsModule;
import im.actor.core.modules.ModuleContext;
import im.actor.core.modules.sequence.processor.SequenceProcessor;
import im.actor.core.network.parser.Update;
import im.actor.runtime.Log;
import im.actor.runtime.actors.messages.Void;
import im.actor.runtime.promise.Promise;
import im.actor.runtime.Runtime;

public class RawProcessor extends AbsModule implements SequenceProcessor {

    private final RawUpdatesHandler rawUpdatesHandler;

    public RawProcessor(ModuleContext context) {
        super(context);
        rawUpdatesHandler = context().getConfiguration().getRawUpdatesHandler();

    }

    @Override
    public Promise<Void> process(Update update) {
        if (update instanceof UpdateRawUpdate && rawUpdatesHandler != null) {
            return new Promise<>(resolver -> {
                Runtime.dispatch(() -> {
                    try {
                        Promise<Void> promise = rawUpdatesHandler.onRawUpdate((UpdateRawUpdate) update);
                        if (promise != null) {
                            promise.pipeTo(resolver);
                        }
                    } catch (Exception e) {
                        Log.e("RawUpdateHandler", e);
                    } finally {
                        resolver.result(null);
                    }
                });
            });
        }
        return null;
    }
}
