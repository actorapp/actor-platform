package im.actor.core.modules.encryption;

import java.util.ArrayList;

import im.actor.core.api.ApiEncryptionKey;
import im.actor.core.api.ApiUserOutPeer;
import im.actor.core.api.rpc.RequestLoadEphermalPublicKeys;
import im.actor.core.api.rpc.RequestLoadPublicKey;
import im.actor.core.api.rpc.ResponsePublicKeys;
import im.actor.core.modules.ModuleContext;
import im.actor.core.modules.encryption.entity.OwnPrivateKey;
import im.actor.core.modules.encryption.entity.UserKeysGroup;
import im.actor.core.modules.encryption.entity.UserPublicKey;
import im.actor.core.network.RpcCallback;
import im.actor.core.network.RpcException;
import im.actor.core.util.Hex;
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

public class EncryptedSessionActor extends ModuleActor {

    private final String TAG;

    private final int uid;
    private final UserKeysGroup encryptionKeyGroup;

    private OwnPrivateKey ownIdentityKey;
    private OwnPrivateKey ownEphermalKey0;
    private UserPublicKey theirIdentityKey;
    private UserPublicKey theirEphermalKey0;

    private OwnPrivateKey prevOwnKey;
    private OwnPrivateKey currentOwnKey;
    private UserPublicKey currentTheirKey;

    private byte[] rootChainKey;

    private int outIndex = 0;
    private int inIndex = 0;

    private boolean isReady = false;

    public EncryptedSessionActor(ModuleContext context, int uid, UserKeysGroup encryptionKeyGroup) {
        super(context);
        this.TAG = "EncryptionSessionActor#" + uid + "_" + encryptionKeyGroup.getKeyGroupId();
        this.uid = uid;
        this.encryptionKeyGroup = encryptionKeyGroup;
        this.theirIdentityKey = encryptionKeyGroup.getIdentityKey();
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
                    currentOwnKey = new OwnPrivateKey(RandomUtils.nextRid(), "curve25519", Curve25519.keyGenPrivate(Crypto.randomBytes(64)));
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

                    ApiEncryptionKey encryptionKey = response.getPublicKey().get(RandomUtils.randomId(response.getPublicKey().size()));
                    theirEphermalKey0 = new UserPublicKey(encryptionKey.getKeyId(), encryptionKey.getKeyAlg(), encryptionKey.getKeyMaterial());
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
                    currentTheirKey = new UserPublicKey(encryptionKey.getKeyId(), encryptionKey.getKeyAlg(), encryptionKey.getKeyMaterial());
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
                new RatchetPrivateKey(ownIdentityKey.getKey()),
                new RatchetPrivateKey(ownEphermalKey0.getKey()),
                new RatchetPublicKey(theirIdentityKey.getPublicKey()),
                new RatchetPublicKey(theirEphermalKey0.getPublicKey()));
        rootChainKey = RatchetRootChainKey.makeRootChainKey(
                new RatchetPrivateKey(ownEphermalKey0.getKey()),
                new RatchetPublicKey(theirEphermalKey0.getPublicKey()),
                master_secret);

        unstashAll();
    }

    private void onEncrypt(byte[] data, Future future) {

        ActorBoxKey ratchetMessageKey = RatchetMessageKey.buildKey(rootChainKey, 0);

        byte[] header = ByteStrings.merge(
                ByteStrings.intToBytes(encryptionKeyGroup.getKeyGroupId()),
                ByteStrings.longToBytes(ownEphermalKey0.getKeyId()), /*Alice Initial Ephermal*/
                ByteStrings.longToBytes(theirEphermalKey0.getKeyId()), /*Bob Initial Ephermal*/
                Curve25519.keyGenPublic(currentOwnKey.getKey()),
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


        int keyGroupId = ByteStrings.bytesToInt(pkg, 0);
        long ownEphermalKey0Id = ByteStrings.bytesToLong(pkg, 4);
        long theirEphermalKey0Id = ByteStrings.bytesToLong(pkg, 12);
        byte[] ownEphermalKey = ByteStrings.substring(pkg, 20, 32);
        byte[] theirEphermalKey = ByteStrings.substring(pkg, 52, 32);
        int messageIndex = ByteStrings.bytesToInt(pkg, 84);

        Log.d(TAG, "onEncrypt: " + Hex.toHex(pkg));
        Log.d(TAG, "onEncrypt:key group id: " + encryptionKeyGroup.getKeyGroupId());
        Log.d(TAG, "onEncrypt:ownEphermalKey0Id: " + ownEphermalKey0.getKeyId());
        Log.d(TAG, "onEncrypt:theirEphermalKey0Id: " + theirEphermalKey0.getKeyId());
        Log.d(TAG, "onEncrypt:messageIndex: " + outIndex);


        Log.d(TAG, "onEncrypt:2key group id: " + keyGroupId);
        Log.d(TAG, "onEncrypt:2ownEphermalKey0Id: " + ownEphermalKey0Id);
        Log.d(TAG, "onEncrypt:2theirEphermalKey0Id: " + theirEphermalKey0Id);
        Log.d(TAG, "onEncrypt:2messageIndex: " + messageIndex);

        future.onResult(new EncryptedPackageRes(pkg));
    }

    private void onDecrypt(final byte[] data, final Future future) {
        final int keyGroupId = ByteStrings.bytesToInt(data, 0);
        final long ownEphermalKey0Id = ByteStrings.bytesToLong(data, 4);
        final long theirEphermalKey0Id = ByteStrings.bytesToLong(data, 12);
        final byte[] ownEphermalKey = ByteStrings.substring(data, 20, 32);
        final byte[] theirEphermalKey = ByteStrings.substring(data, 52, 32);
        final int messageIndex = ByteStrings.bytesToInt(data, 84);

        Log.d(TAG, "onDecrypt: " + Hex.toHex(data));
        Log.d(TAG, "onDecrypt:key group id: " + keyGroupId + ", " + data.length);
        Log.d(TAG, "onDecrypt:ownEphermalKey0Id: " + ownEphermalKey0Id);
        Log.d(TAG, "onDecrypt:theirEphermalKey0Id: " + theirEphermalKey0Id);
        Log.d(TAG, "onDecrypt:messageIndex: " + messageIndex);

        ask(context().getEncryption().getKeyManager(), new KeyManagerActor.FetchEphemeralPrivateKey(theirEphermalKey), new AskCallback() {
            @Override
            public void onResult(Object obj) {

                Log.d(TAG, "onDecrypt:onResultEphermal");

                final KeyManagerActor.FetchEphemeralPrivateKeyRes ownEphermalKey
                        = (KeyManagerActor.FetchEphemeralPrivateKeyRes) obj;

                ask(context().getEncryption().getKeyManager(), new KeyManagerActor.FetchEphemeralPrivateKeyById(theirEphermalKey0Id),
                        new AskCallback() {
                            @Override
                            public void onResult(Object obj) {

                                Log.d(TAG, "onDecrypt:onResultPrivate");

                                final KeyManagerActor.FetchEphemeralPrivateKeyRes ownEphermalKey0
                                        = (KeyManagerActor.FetchEphemeralPrivateKeyRes) obj;

                                ArrayList<Long> keys = new ArrayList<Long>();
                                keys.add(ownEphermalKey0Id);

                                request(new RequestLoadPublicKey(new ApiUserOutPeer(uid, getUser(uid).getAccessHash()), keyGroupId, keys), new RpcCallback<ResponsePublicKeys>() {
                                    @Override
                                    public void onResult(ResponsePublicKeys response) {
                                        Log.d(TAG, "onDecrypt:RequestLoadPublicKey");
                                        onDecrypt(data, ownEphermalKey0.getPrivateKey(),
                                                ownEphermalKey.getPrivateKey(),
                                                theirEphermalKey,
                                                response.getPublicKey().get(0).getKeyMaterial(),
                                                messageIndex,
                                                future);
                                    }

                                    @Override
                                    public void onError(RpcException e) {
                                        Log.d(TAG, "onDecrypt:RequestLoadPublicKey:onError");
                                        future.onError(e);
                                    }
                                });

                            }

                            @Override
                            public void onError(Exception e) {
                                Log.d(TAG, "onDecrypt:onResultPrivate:onError");
                                future.onError(e);
                            }
                        });
            }

            @Override
            public void onError(Exception e) {
                Log.d(TAG, "onDecrypt:onResultEphermal:onError");
                future.onError(e);
            }
        });

        // future.onResult();
    }

    private void onDecrypt(byte[] data,
                           byte[] ownEphemeralPrivateKey0,
                           byte[] ownEphemeralPrivateKey,
                           byte[] theirEphemeralKey0,
                           byte[] theirEphemeralKey,
                           int index,
                           Future future) {

        byte[] ms = RatchetMasterSecret.calculateMasterSecret(
                new RatchetPrivateKey(ownIdentityKey.getKey()),
                new RatchetPrivateKey(ownEphemeralPrivateKey0),
                new RatchetPublicKey(encryptionKeyGroup.getIdentityKey().getPublicKey()),
                new RatchetPublicKey(theirEphemeralKey0));

        byte[] rc = RatchetRootChainKey.makeRootChainKey(
                new RatchetPrivateKey(ownEphemeralPrivateKey),
                new RatchetPublicKey(theirEphemeralKey),
                ms);

        ActorBoxKey ratchetMessageKey = RatchetMessageKey.buildKey(rc, index);


        future.onResult();
    }

    @Override
    public void onReceive(Object message) {
        Log.d(TAG, "msg: " + message);
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
        } else if (message instanceof DecryptPackage) {
            DecryptPackage decryptPackage = (DecryptPackage) message;
            onDecrypt(decryptPackage.getData(), future);
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

    public static class DecryptPackage {

        private byte[] data;

        public DecryptPackage(byte[] data) {
            this.data = data;
        }

        public byte[] getData() {
            return data;
        }
    }
}