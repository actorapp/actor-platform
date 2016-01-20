package im.actor.core.modules.internal.encryption;

import java.io.IOException;
import java.util.ArrayList;

import im.actor.core.modules.ModuleContext;
import im.actor.core.modules.internal.encryption.entity.EncryptionKey;
import im.actor.core.modules.internal.encryption.entity.PrivateKeyStorage;
import im.actor.core.modules.utils.ModuleActor;
import im.actor.runtime.Crypto;
import im.actor.runtime.crypto.Curve25519;
import im.actor.sdk.util.Randoms;

public class KeyManagerActor extends ModuleActor {

    private static final String PRIVATE_KEYS = "private_keys";

    private PrivateKeyStorage privateKeyStorage;

    public KeyManagerActor(ModuleContext context) {
        super(context);
    }

    @Override
    public void preStart() {
        byte[] data = preferences().getBytes(PRIVATE_KEYS);
        if (data != null) {
            try {
                privateKeyStorage = new PrivateKeyStorage(data);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        if (privateKeyStorage == null) {
            EncryptionKey identityKey = new EncryptionKey(Randoms.randomId(),
                    Curve25519.keyGen(Crypto.randomBytes(64)));
            ArrayList<EncryptionKey> keyPairs = new ArrayList<EncryptionKey>();
            keyPairs.add(new EncryptionKey(Randoms.randomId(),
                    Curve25519.keyGen(Crypto.randomBytes(64))));

            privateKeyStorage = new PrivateKeyStorage(identityKey, keyPairs, 0);
            preferences().putBytes(PRIVATE_KEYS, data);
        }
    }
}