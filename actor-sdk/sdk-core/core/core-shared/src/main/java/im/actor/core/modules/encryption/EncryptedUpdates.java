package im.actor.core.modules.encryption;

import im.actor.core.api.ApiEncryptedContent;
import im.actor.core.modules.AbsModule;
import im.actor.core.modules.ModuleContext;
import im.actor.core.modules.messaging.MessagesProcessorEncrypted;
import im.actor.runtime.Log;
import im.actor.runtime.actors.messages.Void;
import im.actor.runtime.promise.Promise;

public class EncryptedUpdates extends AbsModule {

    private EncryptedSequenceProcessor[] processors;

    public EncryptedUpdates(ModuleContext context) {
        super(context);

        processors = new EncryptedSequenceProcessor[]{
                new MessagesProcessorEncrypted(context)
        };
    }

    public Promise<Void> onUpdate(int senderId, long date, ApiEncryptedContent update) {
        Log.d("EncryptedUpdates", "Handling update (from #" + senderId + "): " + update);

        Promise<Void> res = null;
        for (EncryptedSequenceProcessor s : processors) {
            res = s.onUpdate(senderId, date, update);
            if (res != null) {
                break;
            }
        }
        if (res == null) {
            res = Promise.success(null);
        }
        return res;
    }
}
