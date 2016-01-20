package im.actor.core.modules.updates;

import im.actor.core.modules.ModuleContext;
import im.actor.core.modules.utils.ModuleActor;
import im.actor.core.network.parser.Update;
import im.actor.runtime.Log;
import im.actor.runtime.actors.messages.StashIgnore;

public class SequenceHandlerActor extends ModuleActor {

    private static final String TAG = "SequenceHandlerActor";

    private UpdateProcessor processor;

    public SequenceHandlerActor(UpdateProcessor processor, ModuleContext context) {
        super(context);

        this.processor = processor;
    }

    private void onWeakUpdateReceived(Update update, long date) {
        Log.d(TAG, "Processing weak update...");
        this.processor.processWeakUpdate(update, date);

        Log.d(TAG, "Unlocking sequence in 1500 ms");
        sender().send(new StashIgnore(new SequenceActor.UpdateProcessed()));
    }

    @Override
    public void onReceive(Object message) {
        if (message instanceof WeakUpdate) {
            onWeakUpdateReceived(((WeakUpdate) message).getUpdate(),
                    ((WeakUpdate) message).getDate());
        } else {
            super.onReceive(message);
        }
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
