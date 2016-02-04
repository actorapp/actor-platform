package im.actor.core.modules.encryption;

import im.actor.core.api.ApiEncryptedBox;
import im.actor.core.api.ApiEncryptedData;
import im.actor.core.modules.ModuleContext;
import im.actor.runtime.actors.ActorInterface;
import im.actor.runtime.actors.ActorRef;
import im.actor.runtime.promise.Promise;

import static im.actor.runtime.actors.ActorSystem.system;

public class EncryptedInt extends ActorInterface {

    public EncryptedInt(ModuleContext context) {
        super(system().actorOf("encryption/messaging",
                EncryptedActor.CONSTRUCTOR(context)));
    }

    public Promise<EncryptedActor.PlainTextPackage> doDecrypt(int uid, ApiEncryptedBox box) {
        return ask(new EncryptedActor.DoDecryptPackage(uid, box));
    }

    public Promise<EncryptedActor.CipherTextPackage> doEncrypt(int uid, ApiEncryptedData data) {
        return ask(new EncryptedActor.DoEncryptPackage(uid, data));
    }
}
