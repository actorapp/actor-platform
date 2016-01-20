package im.actor.core.modules.internal.encryption;

import java.util.ArrayList;

import im.actor.core.modules.ModuleContext;
import im.actor.core.modules.internal.encryption.entity.EncryptedBox;
import im.actor.core.modules.internal.encryption.entity.EncryptionKey;
import im.actor.core.util.ModuleActor;
import im.actor.core.util.RandomUtils;
import im.actor.runtime.Crypto;
import im.actor.runtime.Log;
import im.actor.runtime.actors.Future;
import im.actor.runtime.actors.ask.AskCallback;
import im.actor.runtime.actors.ask.AskRequest;
import im.actor.runtime.crypto.Curve25519;

public class EncryptedStateActor extends ModuleActor {

    private static final String TAG = "EncryptedStateActor";

    private int uid;
    private EncryptionKey ownIdentityKey;
    private EncryptionKey theirIdentityKey;
    private EncryptionKey ownEphermalKey0;
    private EncryptionKey theirEphermalKey0;

    private ArrayList<EncryptionKey> prevOwnKeys = new ArrayList<EncryptionKey>();
    private ArrayList<EncryptionKey> prevTheirKeys = new ArrayList<EncryptionKey>();

    private EncryptionKey currentOwnKey;
    private EncryptionKey currentTheirKey;

    private byte[] rootChainKey;

    private boolean isReady = false;

    public EncryptedStateActor(int uid, ModuleContext context) {
        super(context);
        this.uid = uid;
    }

    @Override
    public void preStart() {
        super.preStart();

        if (ownIdentityKey == null || ownEphermalKey0 == null || currentOwnKey == null) {
            Log.d(TAG, "Loading own keys for conversation");
            ask(context().getEncryption().getKeyManager(), new KeyManagerActor.FetchOwnKey(), new AskCallback() {
                @Override
                public void onResult(Object obj) {
                    Log.d(TAG, "Own keys loaded");
                    KeyManagerActor.FetchOwnKeyResult res = (KeyManagerActor.FetchOwnKeyResult) obj;
                    ownIdentityKey = res.getIdentityKey();
                    ownEphermalKey0 = res.getEphemeralKey();
                    currentOwnKey = new EncryptionKey(RandomUtils.nextRid(), Curve25519.keyGen(Crypto.randomBytes(64)));
                    onOwnReady();
                }

                @Override
                public void onError(Exception e) {
                    // Nothing to do
                    Log.w(TAG, "Own keys error");
                    Log.e(TAG, e);

                }
            });
        } else {
            onOwnReady();
        }
    }

    private void onOwnReady() {
        Log.w(TAG, "Own keys ready");
    }

    private void onTheirReady() {

    }

    private void doEncrypt(byte[] data, Future future) {

    }

    private void doDecrypt(EncryptedBox data, Future future) {

    }

    @Override
    public boolean onAsk(Object message, Future future) {
        if (message instanceof EncryptPackage) {
            doEncrypt(((EncryptPackage) message).getData(), future);
            return false;
        } else if (message instanceof DecryptPackage) {
            doDecrypt(((DecryptPackage) message).getEncryptedBox(), future);
            return false;
        } else {
            return super.onAsk(message, future);
        }
    }

    @Override
    public void onReceive(Object message) {
        if (!isReady && message instanceof AskRequest) {
            stash();
            return;
        }
        super.onReceive(message);
    }

    public static class EncryptPackage {
        private byte[] data;

        public EncryptPackage(byte[] data) {
            this.data = data;
        }

        public byte[] getData() {
            return data;
        }
    }

    public static class DecryptPackage {

        private EncryptedBox encryptedBox;

        public DecryptPackage(EncryptedBox encryptedBox) {
            this.encryptedBox = encryptedBox;
        }

        public EncryptedBox getEncryptedBox() {
            return encryptedBox;
        }
    }
}
