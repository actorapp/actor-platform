package im.actor.core.modules.encryption;

import java.util.ArrayList;

import im.actor.core.api.ApiEncryptionKey;
import im.actor.core.api.ApiEncryptionKeyGroup;
import im.actor.core.api.ApiUserOutPeer;
import im.actor.core.api.rpc.RequestLoadEphermalPublicKeys;
import im.actor.core.api.rpc.ResponsePublicKeys;
import im.actor.core.modules.ModuleContext;
import im.actor.core.modules.encryption.entity.EncryptionKey;
import im.actor.core.network.RpcCallback;
import im.actor.core.network.RpcException;
import im.actor.core.util.ModuleActor;
import im.actor.core.util.RandomUtils;
import im.actor.runtime.Crypto;
import im.actor.runtime.Log;
import im.actor.runtime.actors.Future;
import im.actor.runtime.actors.ask.AskCallback;
import im.actor.runtime.actors.ask.AskRequest;
import im.actor.runtime.crypto.Curve25519;
import im.actor.runtime.crypto.IntegrityException;
import im.actor.runtime.crypto.box.ActorBox;
import im.actor.runtime.crypto.box.ActorBoxKey;
import im.actor.runtime.crypto.primitives.util.ByteStrings;
import im.actor.runtime.crypto.ratchet.RatchetMasterSecret;
import im.actor.runtime.crypto.ratchet.RatchetMessageKey;
import im.actor.runtime.crypto.ratchet.RatchetPrivateKey;
import im.actor.runtime.crypto.ratchet.RatchetPublicKey;
import im.actor.runtime.crypto.ratchet.RatchetRootChainKey;

public class EncryptionSessionActor extends ModuleActor {

    private final String TAG;

    private final int uid;
    private final ApiEncryptionKeyGroup encryptionKeyGroup;

    private EncryptionKey ownIdentityKey;
    private EncryptionKey theirIdentityKey;
    private EncryptionKey ownEphermalKey0;
    private EncryptionKey theirEphermalKey0;

    private ArrayList<EncryptionKey> prevOwnKeys = new ArrayList<EncryptionKey>();
    private ArrayList<EncryptionKey> prevTheirKeys = new ArrayList<EncryptionKey>();

    private EncryptionKey currentOwnKey;
    private EncryptionKey currentTheirKey;

    private byte[] rootChainKey;

    private int outIndex = 0;

    private boolean isReady = false;

    public EncryptionSessionActor(ModuleContext context, int uid, ApiEncryptionKeyGroup encryptionKeyGroup) {
        super(context);
        this.TAG = "EncryptionSessionActor#" + uid + "_" + encryptionKeyGroup.getKeyGroupId();
        this.uid = uid;
        this.encryptionKeyGroup = encryptionKeyGroup;
        this.theirIdentityKey = new EncryptionKey(encryptionKeyGroup.getIdentityKey().getKeyId(), encryptionKeyGroup.getIdentityKey().getKeyAlg(), encryptionKeyGroup.getIdentityKey().getKeyMaterial(), null);
    }

    @Override
    public void preStart() {
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

        if (theirEphermalKey0 == null) {
            request(new RequestLoadEphermalPublicKeys(new ApiUserOutPeer(uid, getUser(uid).getAccessHash()), encryptionKeyGroup.getKeyGroupId()), new RpcCallback<ResponsePublicKeys>() {
                @Override
                public void onResult(ResponsePublicKeys response) {
                    if (response.getPublicKey().size() == 0) {
                        Log.w(TAG, "No ephemeral keys found");
                        return;
                    }

                    ApiEncryptionKey encryptionKey = response.getPublicKey().get(0);
                    theirEphermalKey0 = new EncryptionKey(encryptionKey.getKeyId(), encryptionKey.getKeyAlg(), encryptionKey.getKeyMaterial(), null);
                    onTheirReady0();
                }

                @Override
                public void onError(RpcException e) {
                    // Nothing to do
                    Log.w(TAG, "Their ephemeral error");
                    Log.e(TAG, e);
                }
            });
        } else {
            onTheirReady0();
        }
    }

    private void onTheirReady0() {
        Log.w(TAG, "Their identity ephemeral keys ready");

        if (currentTheirKey == null) {
            request(new RequestLoadEphermalPublicKeys(new ApiUserOutPeer(uid, getUser(uid).getAccessHash()), encryptionKeyGroup.getKeyGroupId()), new RpcCallback<ResponsePublicKeys>() {
                @Override
                public void onResult(ResponsePublicKeys response) {
                    if (response.getPublicKey().size() == 0) {
                        Log.w(TAG, "No ephemeral keys found");
                        return;
                    }

                    ApiEncryptionKey encryptionKey = response.getPublicKey().get(RandomUtils.randomId(response.getPublicKey().size()));
                    currentTheirKey = new EncryptionKey(encryptionKey.getKeyId(), encryptionKey.getKeyAlg(), encryptionKey.getKeyMaterial(), null);
                    allSet();
                }

                @Override
                public void onError(RpcException e) {
                    // Nothing to do
                    Log.w(TAG, "Their ephemeral error");
                    Log.e(TAG, e);
                }
            });
        } else {
            onTheirReady0();
        }
    }

    private void allSet() {
        Log.d(TAG, "All keys are ready");
        isReady = true;

        byte[] master_secret = RatchetMasterSecret.calculateMasterSecret(
                new RatchetPrivateKey(ownIdentityKey.getPrivateKey()),
                new RatchetPrivateKey(ownEphermalKey0.getPrivateKey()),
                new RatchetPublicKey(theirIdentityKey.getPublicKey()),
                new RatchetPublicKey(theirEphermalKey0.getPublicKey()));
        rootChainKey = RatchetRootChainKey.makeRootChainKey(
                new RatchetPrivateKey(ownEphermalKey0.getPrivateKey()),
                new RatchetPublicKey(theirEphermalKey0.getPublicKey()),
                master_secret);

        unstashAll();
    }

    private void onEncrypt(byte[] data, Future future) {

        ActorBoxKey ratchetMessageKey = RatchetMessageKey.buildKey(rootChainKey, 0);

        byte[] header = ByteStrings.merge(
                ByteStrings.longToBytes(ownEphermalKey0.getKeyId()), /*Alice Initial Ephermal*/
                ByteStrings.longToBytes(theirEphermalKey0.getKeyId()), /*Bob Initial Ephermal*/
                currentOwnKey.getPublicKey(),
                currentTheirKey.getPublicKey(),
                ByteStrings.intToBytes(outIndex++)); /* Message Index */

        byte[] encrypted;
        try {
            encrypted = ActorBox.closeBox(header, data, Crypto.randomBytes(32), ratchetMessageKey);
        } catch (IntegrityException e) {
            e.printStackTrace();
            future.onError(e);
            return;
        }

        byte[] pkg = ByteStrings.merge(header, encrypted);

        future.onResult(new EncryptedPackageRes(pkg));
    }

    @Override
    public void onReceive(Object message) {
        if (!isReady && message instanceof AskRequest) {
            stash();
            return;
        }
        super.onReceive(message);
    }

    @Override
    public boolean onAsk(Object message, Future future) {
        if (message instanceof EncryptPackage) {
            onEncrypt(((EncryptPackage) message).getData(), future);
            return false;
        } else {
            return super.onAsk(message, future);
        }
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

    public static class EncryptedPackageRes {

        private byte[] data;

        public EncryptedPackageRes(byte[] data) {
            this.data = data;
        }

        public byte[] getData() {
            return data;
        }
    }
}