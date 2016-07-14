package im.actor.core.modules.encryption;

import im.actor.core.api.ApiEncryptedContent;
import im.actor.core.api.ApiEncryptedDeleteContent;
import im.actor.core.api.ApiEncryptedEditContent;
import im.actor.core.api.ApiEncryptedMessageContent;
import im.actor.core.api.updates.UpdateEncryptedPackage;
import im.actor.core.api.updates.UpdatePublicKeyGroupAdded;
import im.actor.core.api.updates.UpdatePublicKeyGroupRemoved;
import im.actor.core.entity.Message;
import im.actor.core.entity.MessageState;
import im.actor.core.entity.Peer;
import im.actor.core.entity.content.AbsContent;
import im.actor.core.modules.AbsModule;
import im.actor.core.modules.ModuleContext;
import im.actor.core.modules.messaging.MessagesProcessorEncrypted;
import im.actor.core.modules.sequence.processor.SequenceProcessor;
import im.actor.core.network.parser.Update;
import im.actor.runtime.Log;
import im.actor.runtime.actors.messages.Void;
import im.actor.runtime.function.Function;
import im.actor.runtime.promise.Promise;

public class EncryptionProcessor extends AbsModule implements SequenceProcessor {

    private EncryptedSequenceProcessor[] processors;

    public EncryptionProcessor(ModuleContext context) {
        super(context);

        processors = new EncryptedSequenceProcessor[]{
                new MessagesProcessorEncrypted(context)
        };
    }

    @Override
    public Promise<Void> process(Update update) {
        if (update instanceof UpdatePublicKeyGroupAdded) {
            UpdatePublicKeyGroupAdded groupAdded = (UpdatePublicKeyGroupAdded) update;
            return context().getEncryption()
                    .getKeyManager()
                    .onKeyGroupAdded(groupAdded.getUid(), groupAdded.getKeyGroup());
        } else if (update instanceof UpdatePublicKeyGroupRemoved) {
            UpdatePublicKeyGroupRemoved groupRemoved = (UpdatePublicKeyGroupRemoved) update;
            return context().getEncryption()
                    .getKeyManager()
                    .onKeyGroupRemoved(groupRemoved.getUid(), groupRemoved.getKeyGroupId());
        } else if (update instanceof UpdateEncryptedPackage) {
            UpdateEncryptedPackage encryptedPackage = (UpdateEncryptedPackage) update;
            return context().getEncryption()
                    .decrypt(encryptedPackage.getSenderId(), encryptedPackage.getEncryptedBox())
                    .flatMap(message -> {
                        return process(encryptedPackage.getSenderId(), encryptedPackage.getDate(), message);
                    }).fallback(e -> Promise.success(null));
        }
        return null;
    }

    public Promise<Void> process(int senderId, long date, ApiEncryptedContent update) {

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
