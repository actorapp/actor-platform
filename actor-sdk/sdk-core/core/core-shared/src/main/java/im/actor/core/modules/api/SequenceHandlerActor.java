package im.actor.core.modules.api;

import im.actor.core.modules.ModuleContext;
import im.actor.core.modules.updates.UpdateProcessor;
import im.actor.core.util.ModuleActor;
import im.actor.core.network.parser.Update;
import im.actor.runtime.actors.Future;

public class SequenceHandlerActor extends ModuleActor {

    private static final String TAG = "SequenceHandlerActor";

    private UpdateProcessor processor;

    public SequenceHandlerActor(UpdateProcessor processor, ModuleContext context) {
        super(context);

        this.processor = processor;
    }

    @Override
    public boolean onAsk(Object message, Future future) {
        if (message instanceof WeakUpdate) {
            WeakUpdate weakUpdate = (WeakUpdate) message;
            onWeakUpdateReceived(weakUpdate.getUpdate(), weakUpdate.getDate());
            return true;
        } else {
            return super.onAsk(message, future);
        }
    }

    private void onWeakUpdateReceived(Update update, long date) {
        this.processor.processWeakUpdate(update, date);
    }

    public static class WeakUpdate {

        private Update update;
        private long date;

        public WeakUpdate(Update update, long date) {
            this.update = update;
            this.date = date;
        }

        public long getDate() {
            return date;
        }

        public Update getUpdate() {
            return update;
        }
    }
}
