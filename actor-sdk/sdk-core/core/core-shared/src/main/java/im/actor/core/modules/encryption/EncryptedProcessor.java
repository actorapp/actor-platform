package im.actor.core.modules.encryption;

import im.actor.core.api.updates.UpdateEncryptedPackage;
import im.actor.core.api.updates.UpdatePublicKeyGroupAdded;
import im.actor.core.api.updates.UpdatePublicKeyGroupRemoved;
import im.actor.core.modules.AbsModule;
import im.actor.core.modules.ModuleContext;
import im.actor.core.modules.encryption.KeyManagerActor;
import im.actor.core.modules.sequence.Processor;
import im.actor.runtime.actors.ActorRef;
import im.actor.runtime.function.Consumer;

public class EncryptedProcessor extends AbsModule implements Processor {

    public EncryptedProcessor(ModuleContext context) {
        super(context);
    }

    @Override
    public boolean process(ActorRef ref, Object update) {
        if (update instanceof UpdatePublicKeyGroupAdded) {
            context().getEncryption().getKeyManager().send(new KeyManagerActor.PublicKeysGroupAdded(
                    ((UpdatePublicKeyGroupAdded) update).getUid(),
                    ((UpdatePublicKeyGroupAdded) update).getKeyGroup()
            ));
            return true;
        } else if (update instanceof UpdatePublicKeyGroupRemoved) {
            context().getEncryption().getKeyManager().send(new KeyManagerActor.PublicKeysGroupRemoved(
                    ((UpdatePublicKeyGroupRemoved) update).getUid(),
                    ((UpdatePublicKeyGroupRemoved) update).getKeyGroupId()
            ));
            return true;
        } else if (update instanceof UpdateEncryptedPackage) {
            context().getEncryption().getEncrypted().doDecrypt(((UpdateEncryptedPackage) update).getSenderId(),
                    ((UpdateEncryptedPackage) update).getEncryptedBox()).then(new Consumer<EncryptedActor.PlainTextPackage>() {
                @Override
                public void apply(EncryptedActor.PlainTextPackage plainTextPackage) {

                }
            }).failure(new Consumer<Exception>() {
                @Override
                public void apply(Exception e) {

                }
            }).done(ref);
            return true;
        }
        return false;
    }
}
