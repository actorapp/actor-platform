package im.actor.core.modules.updates;

import im.actor.core.api.updates.UpdateEncryptedPackage;
import im.actor.core.api.updates.UpdatePublicKeyGroupAdded;
import im.actor.core.api.updates.UpdatePublicKeyGroupChanged;
import im.actor.core.api.updates.UpdatePublicKeyGroupRemoved;
import im.actor.core.modules.AbsModule;
import im.actor.core.modules.ModuleContext;
import im.actor.core.modules.encryption.EncryptedMsgActor;
import im.actor.core.modules.encryption.KeyManagerActor;
import im.actor.core.modules.sequence.Processor;

public class EncryptedProcessor extends AbsModule implements Processor {

    public EncryptedProcessor(ModuleContext context) {
        super(context);
    }

    @Override
    public boolean process(Object update) {
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
            
        }
        return false;
    }
}
