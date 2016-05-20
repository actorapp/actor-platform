package im.actor.core.modules.encryption;

import im.actor.core.modules.AbsModule;
import im.actor.core.modules.ModuleContext;
import im.actor.core.modules.sequence.processor.SequenceProcessor;
import im.actor.core.network.parser.Update;
import im.actor.runtime.actors.messages.Void;
import im.actor.runtime.promise.Promise;

public class EncryptedProcessor extends AbsModule implements SequenceProcessor {

    public EncryptedProcessor(ModuleContext context) {
        super(context);
    }

    @Override
    public Promise<Void> process(Update update) {
//        if (update instanceof UpdatePublicKeyGroupAdded) {
//            context().getEncryption().getKeyManager().send(new KeyManagerActor.PublicKeysGroupAdded(
//                    ((UpdatePublicKeyGroupAdded) update).getUid(),
//                    ((UpdatePublicKeyGroupAdded) update).getKeyGroup()
//            ));
//            return true;
//        } else if (update instanceof UpdatePublicKeyGroupRemoved) {
//            context().getEncryption().getKeyManager().send(new KeyManagerActor.PublicKeysGroupRemoved(
//                    ((UpdatePublicKeyGroupRemoved) update).getUid(),
//                    ((UpdatePublicKeyGroupRemoved) update).getKeyGroupId()
//            ));
//            return true;
//        } else if (update instanceof UpdateEncryptedPackage) {
//
//        }
        return null;
    }
}
