package im.actor.core.modules.encryption;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import im.actor.core.api.ApiEncryptionKey;
import im.actor.core.api.ApiEncryptionKeyGroup;
import im.actor.core.api.ApiEncryptionKeySignature;
import im.actor.core.api.ApiUserOutPeer;
import im.actor.core.api.rpc.RequestCreateNewKeyGroup;
import im.actor.core.api.rpc.RequestLoadPublicKeyGroups;
import im.actor.core.api.rpc.RequestUploadEphermalKey;
import im.actor.core.api.rpc.ResponseCreateNewKeyGroup;
import im.actor.core.api.rpc.ResponsePublicKeyGroups;
import im.actor.core.api.rpc.ResponseVoid;
import im.actor.core.entity.User;
import im.actor.core.modules.ModuleContext;
import im.actor.core.modules.encryption.entity.OwnKeys;
import im.actor.core.modules.encryption.entity.OwnPrivateKey;
import im.actor.core.modules.encryption.entity.OwnPrivateKeyUploadable;
import im.actor.core.modules.encryption.entity.UserKeys;
import im.actor.core.modules.encryption.entity.UserKeysGroup;
import im.actor.core.modules.encryption.entity.UserPublicKey;
import im.actor.core.util.ModuleActor;
import im.actor.core.util.RandomUtils;
import im.actor.core.network.RpcCallback;
import im.actor.core.network.RpcException;
import im.actor.runtime.Crypto;
import im.actor.runtime.Log;
import im.actor.runtime.Storage;
import im.actor.runtime.actors.Future;
import im.actor.runtime.actors.ask.AskRequest;
import im.actor.runtime.crypto.Curve25519;
import im.actor.runtime.crypto.primitives.util.ByteStrings;
import im.actor.runtime.storage.KeyValueStorage;

public class KeyManagerActor extends ModuleActor {

    private static final String TAG = "KeyManagerActor";

    private KeyValueStorage encryptionKeysStorage;

    private HashMap<Integer, UserKeys> cachedUserKeys = new HashMap<Integer, UserKeys>();
    private OwnKeys ownKeys;

    private boolean isReady = false;

    public KeyManagerActor(ModuleContext context) {
        super(context);
    }

    @Override
    public void preStart() {

        encryptionKeysStorage = Storage.createKeyValue("encryption_keys");

        ownKeys = null;
        byte[] ownKeysStorage = encryptionKeysStorage.loadItem(0);
        if (ownKeysStorage != null) {
            try {
                ownKeys = new OwnKeys(ownKeysStorage);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        if (ownKeys == null) {
            byte[] identityPrivate = Curve25519.keyGenPrivate(Crypto.randomBytes(64));
            byte[] key0 = Curve25519.keyGenPrivate(Crypto.randomBytes(64));

            ownKeys = new OwnKeys(0,
                    new OwnPrivateKey(RandomUtils.nextRid(), "curve25519", identityPrivate),
                    new OwnPrivateKey[]{
                            new OwnPrivateKey(RandomUtils.nextRid(), "curve25519", key0)
                    },
                    new OwnPrivateKeyUploadable[0]);
            encryptionKeysStorage.addOrUpdateItem(0, ownKeys.toByteArray());
        }

        if (ownKeys.getKeyGroupId() == 0) {
            OwnPrivateKey privateKey = ownKeys.getIdentityKey();
            byte[] publicKey = Curve25519.keyGenPublic(privateKey.getKey());
            ApiEncryptionKey apiEncryptionKey = new ApiEncryptionKey(
                    privateKey.getKeyId(),
                    privateKey.getKeyAlg(),
                    publicKey,
                    null);
            ArrayList<String> encryption = new ArrayList<String>();
            encryption.add("curve25519");
            encryption.add("Ed25519");
            encryption.add("kuznechik128");
            encryption.add("streebog256");
            encryption.add("sha256");
            encryption.add("sha512");
            encryption.add("aes128");

            ArrayList<ApiEncryptionKey> keys = new ArrayList<ApiEncryptionKey>();
            ArrayList<ApiEncryptionKeySignature> keySignatures = new ArrayList<ApiEncryptionKeySignature>();
            for (OwnPrivateKey key : ownKeys.getKeys()) {
                byte[] publicKey2 = Curve25519.keyGenPublic(privateKey.getKey());
                ApiEncryptionKey apiKey =
                        new ApiEncryptionKey(
                                key.getKeyId(),
                                key.getKeyAlg(),
                                publicKey2,
                                null);
                keys.add(apiKey);


                byte[] signature = Curve25519.calculateSignature(Crypto.randomBytes(64), privateKey.getKey(), apiKey.toByteArray());
                keySignatures.add(
                        new ApiEncryptionKeySignature(
                                key.getKeyId(),
                                "Ed25519",
                                signature));
            }

            request(new RequestCreateNewKeyGroup(apiEncryptionKey, encryption, keys, keySignatures), new RpcCallback<ResponseCreateNewKeyGroup>() {
                @Override
                public void onResult(ResponseCreateNewKeyGroup response) {
                    ownKeys = ownKeys.setGroupId(response.getKeyGroupId());
                    encryptionKeysStorage.addOrUpdateItem(0, ownKeys.toByteArray());
                    onMainKeysReady();
                }

                @Override
                public void onError(RpcException e) {
                    Log.w(TAG, "Keys upload error");
                    Log.e(TAG, e);

                    // Just ignore
                }
            });
        } else {
            onMainKeysReady();
        }
    }

    private void onMainKeysReady() {
        Log.d(TAG, "Main Keys are ready");

        // Generation of missed ephemeral keys
        int missingKeysCount = Math.max(0, Configuration.EPHEMERAL_KEYS_COUNT - ownKeys.getEphemeralKeys().length);
        if (missingKeysCount > 0) {
            OwnPrivateKeyUploadable[] nKeys = new OwnPrivateKeyUploadable[missingKeysCount];
            for (int i = 0; i < missingKeysCount; i++) {
                nKeys[i] = new OwnPrivateKeyUploadable(
                        RandomUtils.nextRid(),
                        "curve25519",
                        Curve25519.keyGenPrivate(Crypto.randomBytes(64)),
                        false);
            }
            ownKeys = ownKeys.appendEphemeralKeys(nKeys);
            encryptionKeysStorage.addOrUpdateItem(0, ownKeys.toByteArray());
        }


        // Uploading ephemeral keys
        // records = ephemeralStorage.loadAllItems();

        final ArrayList<OwnPrivateKeyUploadable> pendingEphermalKeys = new ArrayList<OwnPrivateKeyUploadable>();
        for (OwnPrivateKeyUploadable key : ownKeys.getEphemeralKeys()) {
            if (!key.isUploaded()) {
                pendingEphermalKeys.add(key);
            }
        }

        if (pendingEphermalKeys.size() > 0) {
            final ArrayList<ApiEncryptionKey> uploadingKeys = new ArrayList<ApiEncryptionKey>();
            ArrayList<ApiEncryptionKeySignature> uploadingSignatures = new ArrayList<ApiEncryptionKeySignature>();
            for (OwnPrivateKeyUploadable k : pendingEphermalKeys) {
                ApiEncryptionKey apiKey =
                        new ApiEncryptionKey(
                                k.getKeyId(),
                                k.getKeyAlg(),
                                Curve25519.keyGenPublic(k.getKey()),
                                null);
                uploadingKeys.add(apiKey);


                byte[] signature = Curve25519.calculateSignature(Crypto.randomBytes(64),
                        ownKeys.getIdentityKey().getKey(), apiKey.toByteArray());
                uploadingSignatures.add(
                        new ApiEncryptionKeySignature(
                                k.getKeyId(),
                                "Ed25519",
                                signature));
            }

            request(new RequestUploadEphermalKey(ownKeys.getKeyGroupId(), uploadingKeys, uploadingSignatures), new RpcCallback<ResponseVoid>() {
                @Override
                public void onResult(ResponseVoid response) {
                    ownKeys = ownKeys.markAsUploaded(pendingEphermalKeys.toArray(new OwnPrivateKeyUploadable[pendingEphermalKeys.size()]));
                    encryptionKeysStorage.addOrUpdateItem(0, ownKeys.toByteArray());
                    onAllKeysReady();
                }

                @Override
                public void onError(RpcException e) {
                    Log.w(TAG, "Ephemeral keys upload error");
                    Log.e(TAG, e);

                    // Ignore
                }
            });
        } else {
            onAllKeysReady();
        }
    }

    private void onAllKeysReady() {
        Log.d(TAG, "Ephemeral Keys are ready");

        // Now we can start receiving encrypted messages

        isReady = true;
        unstashAll();
    }

    private void fetchOwnKey(Future future) {
        Log.d(TAG, "fetchOwnKey");
        future.onResult(new FetchOwnKeyResult(ownKeys.getIdentityKey(), ownKeys.pickRandomEphemeralKey()));
    }

    private void fetchKeyGroup(Future future) {
        Log.d(TAG, "fetchKeyGroup");
        future.onResult(new FetchOwnKeyGroupResult(ownKeys.getKeyGroupId()));
    }

    private void fetchEphemeralKey(byte[] publicKey, Future future) {
        Log.d(TAG, "fetchEphemeralKey");
        for (OwnPrivateKey k : ownKeys.getEphemeralKeys()) {
            if (ByteStrings.isEquals(Curve25519.keyGenPublic(k.getKey()), publicKey)) {
                future.onResult(new FetchEphemeralPrivateKeyRes(k.getKey()));
                return;
            }
        }
        future.onError(new RuntimeException("Unable to find ephemeral key"));
    }

    private void fetchEphemeralKey(long keyId, Future future) {
        Log.d(TAG, "fetchEphemeralKey");
        for (OwnPrivateKey k : ownKeys.getEphemeralKeys()) {
            if (k.getKeyId() == keyId) {
                future.onResult(new FetchEphemeralPrivateKeyRes(k.getKey()));
                return;
            }
        }
        future.onError(new RuntimeException("Unable to find ephemeral key"));
    }

    private void fetchUserGroups(final int uid, final Future future) {
        Log.d(TAG, "fetchUserGroups");
        UserKeys userKeys = cachedUserKeys.get(uid);
        if (userKeys == null) {
            byte[] cached = encryptionKeysStorage.loadItem(uid);
            if (cached != null) {
                try {
                    userKeys = new UserKeys(cached);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        if (userKeys != null) {
            Log.d(TAG, "onResult:fast");
            future.onResult(new FetchUserKeyGroupsResponse(userKeys));
            return;
        }

        Log.d(TAG, "Requesting");
        User user = users().getValue(uid);
        request(new RequestLoadPublicKeyGroups(new ApiUserOutPeer(uid, user.getAccessHash())), new RpcCallback<ResponsePublicKeyGroups>() {
            @Override
            public void onResult(ResponsePublicKeyGroups response) {
                Log.d(TAG, "onResult");
                UserKeysGroup[] groups = new UserKeysGroup[response.getPublicKeyGroups().size()];
                for (int i = 0; i < groups.length; i++) {
                    ApiEncryptionKeyGroup encryptionKey = response.getPublicKeyGroups().get(i);

                    // TODO: Validate signatures

                    UserPublicKey identity = new UserPublicKey(
                            encryptionKey.getIdentityKey().getKeyId(),
                            encryptionKey.getIdentityKey().getKeyAlg(),
                            encryptionKey.getIdentityKey().getKeyMaterial());
                    UserPublicKey[] keys = new UserPublicKey[encryptionKey.getKeys().size()];
                    for (int j = 0; j < keys.length; j++) {
                        keys[j] = new UserPublicKey(
                                encryptionKey.getKeys().get(j).getKeyId(),
                                encryptionKey.getKeys().get(j).getKeyAlg(),
                                encryptionKey.getKeys().get(j).getKeyMaterial());
                    }
                    groups[i] = new UserKeysGroup(encryptionKey.getKeyGroupId(), identity, keys, new UserPublicKey[0]);
                }
                UserKeys userKeys1 = new UserKeys(uid, groups);
                encryptionKeysStorage.addOrUpdateItem(uid, userKeys1.toByteArray());
                future.onResult(new FetchUserKeyGroupsResponse(userKeys1));
            }

            @Override
            public void onError(RpcException e) {
                Log.d(TAG, "onError");
                future.onError(e);
            }
        });
    }

    @Override
    public void onReceive(Object message) {
        if (message instanceof AskRequest && !isReady) {
            stash();
            return;
        }
        super.onReceive(message);
    }

    @Override
    public boolean onAsk(Object message, Future future) {
        if (message instanceof FetchOwnKey) {
            fetchOwnKey(future);
            return false;
        } else if (message instanceof FetchOwnKeyGroup) {
            fetchKeyGroup(future);
            return false;
        } else if (message instanceof FetchEphemeralPrivateKey) {
            fetchEphemeralKey(((FetchEphemeralPrivateKey) message).getPublicKey(), future);
            return false;
        } else if (message instanceof FetchEphemeralPrivateKeyById) {
            fetchEphemeralKey(((FetchEphemeralPrivateKeyById) message).getKeyId(), future);
            return false;
        } else if (message instanceof FetchUserKeyGroups) {
            fetchUserGroups(((FetchUserKeyGroups) message).getUid(), future);
            return false;
        }
        return super.onAsk(message, future);
    }

    public static class FetchOwnKey {

    }

    public static class FetchOwnKeyResult {

        private OwnPrivateKey identityKey;
        private OwnPrivateKey ephemeralKey;

        public FetchOwnKeyResult(OwnPrivateKey identityKey, OwnPrivateKey ephemeralKey) {
            this.identityKey = identityKey;
            this.ephemeralKey = ephemeralKey;
        }

        public OwnPrivateKey getIdentityKey() {
            return identityKey;
        }

        public OwnPrivateKey getEphemeralKey() {
            return ephemeralKey;
        }
    }

    public static class FetchOwnKeyGroup {

    }

    public static class FetchOwnKeyGroupResult {
        private int keyGroupId;

        public FetchOwnKeyGroupResult(int keyGroupId) {
            this.keyGroupId = keyGroupId;
        }

        public int getKeyGroupId() {
            return keyGroupId;
        }
    }

    public static class FetchEphemeralPrivateKey {

        private byte[] publicKey;

        public FetchEphemeralPrivateKey(byte[] publicKey) {
            this.publicKey = publicKey;
        }

        public byte[] getPublicKey() {
            return publicKey;
        }
    }

    public static class FetchEphemeralPrivateKeyById {

        private long keyId;

        public FetchEphemeralPrivateKeyById(long keyId) {
            this.keyId = keyId;
        }

        public long getKeyId() {
            return keyId;
        }
    }

    public static class FetchEphemeralPrivateKeyRes {
        private byte[] privateKey;

        public FetchEphemeralPrivateKeyRes(byte[] privateKey) {
            this.privateKey = privateKey;
        }

        public byte[] getPrivateKey() {
            return privateKey;
        }
    }

    public static class FetchUserKeyGroups {
        private int uid;

        public FetchUserKeyGroups(int uid) {
            this.uid = uid;
        }

        public int getUid() {
            return uid;
        }
    }

    public static class FetchUserKeyGroupsResponse {

        private UserKeys userKeys;

        public FetchUserKeyGroupsResponse(UserKeys userKeys) {
            this.userKeys = userKeys;
        }

        public UserKeys getUserKeys() {
            return userKeys;
        }
    }
}