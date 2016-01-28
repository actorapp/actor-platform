package im.actor.core.modules.encryption;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import im.actor.core.api.ApiEncryptionKey;
import im.actor.core.api.ApiEncryptionKeyGroup;
import im.actor.core.api.ApiEncryptionKeySignature;
import im.actor.core.api.ApiUserOutPeer;
import im.actor.core.api.rpc.RequestCreateNewKeyGroup;
import im.actor.core.api.rpc.RequestLoadEphermalPublicKeys;
import im.actor.core.api.rpc.RequestLoadPublicKey;
import im.actor.core.api.rpc.RequestLoadPublicKeyGroups;
import im.actor.core.api.rpc.RequestUploadEphermalKey;
import im.actor.core.api.rpc.ResponseCreateNewKeyGroup;
import im.actor.core.api.rpc.ResponsePublicKeyGroups;
import im.actor.core.api.rpc.ResponsePublicKeys;
import im.actor.core.api.rpc.ResponseVoid;
import im.actor.core.entity.User;
import im.actor.core.modules.ModuleContext;
import im.actor.core.modules.encryption.entity.PrivateKeyStorage;
import im.actor.core.modules.encryption.entity.PrivateKey;
import im.actor.core.modules.encryption.entity.UserKeys;
import im.actor.core.modules.encryption.entity.UserKeysGroup;
import im.actor.core.modules.encryption.entity.PublicKey;
import im.actor.core.util.ModuleActor;
import im.actor.core.util.RandomUtils;
import im.actor.core.network.RpcCallback;
import im.actor.core.network.RpcException;
import im.actor.runtime.Crypto;
import im.actor.runtime.Log;
import im.actor.runtime.Storage;
import im.actor.runtime.actors.ask.AskIntRequest;
import im.actor.runtime.actors.ask.AskMessage;
import im.actor.runtime.actors.ask.AskResult;
import im.actor.runtime.promise.PromiseResolver;
import im.actor.runtime.crypto.Curve25519;
import im.actor.runtime.crypto.primitives.util.ByteStrings;
import im.actor.runtime.crypto.ratchet.RatchetKeySignature;
import im.actor.runtime.function.Consumer;
import im.actor.runtime.storage.KeyValueStorage;

public class KeyManagerActor extends ModuleActor {

    private static final String TAG = "KeyManagerActor";

    private KeyValueStorage encryptionKeysStorage;
    private HashMap<Integer, UserKeys> cachedUserKeys = new HashMap<Integer, UserKeys>();
    private PrivateKeyStorage ownKeys;

    private boolean isReady = false;

    public KeyManagerActor(ModuleContext context) {
        super(context);
    }

    @Override
    public void preStart() {

        Log.d(TAG, "Starting KeyManager...");

        encryptionKeysStorage = Storage.createKeyValue("encryption_keys");

        ownKeys = null;
        byte[] ownKeysStorage = encryptionKeysStorage.loadItem(0);
        if (ownKeysStorage != null) {
            try {
                ownKeys = new PrivateKeyStorage(ownKeysStorage);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        if (ownKeys == null) {
            byte[] identityPrivate = Curve25519.keyGenPrivate(Crypto.randomBytes(64));
            byte[] key0 = Curve25519.keyGenPrivate(Crypto.randomBytes(64));

            ownKeys = new PrivateKeyStorage(0,
                    new PrivateKey(RandomUtils.nextRid(), "curve25519", identityPrivate),
                    new PrivateKey[]{
                            new PrivateKey(RandomUtils.nextRid(), "curve25519", key0)
                    },
                    new PrivateKey[0]);
            encryptionKeysStorage.addOrUpdateItem(0, ownKeys.toByteArray());
        }

        if (ownKeys.getKeyGroupId() == 0) {
            PrivateKey privateKey = ownKeys.getIdentityKey();
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
            for (PrivateKey key : ownKeys.getKeys()) {
                byte[] publicKey2 = Curve25519.keyGenPublic(privateKey.getKey());
                keys.add(new ApiEncryptionKey(
                        key.getKeyId(),
                        key.getKeyAlg(),
                        publicKey2,
                        null));

                byte[] signature = Curve25519.calculateSignature(Crypto.randomBytes(64), privateKey.getKey(),
                        RatchetKeySignature.hashForSignature(key.getKeyId(),
                                key.getKeyAlg(), publicKey2));
                keySignatures.add(
                        new ApiEncryptionKeySignature(
                                key.getKeyId(),
                                "Ed25519",
                                signature));
            }

            Log.d(TAG, "Creation of new key group");
            api(new RequestCreateNewKeyGroup(apiEncryptionKey, encryption, keys, keySignatures)).then(new Consumer<ResponseCreateNewKeyGroup>() {
                @Override
                public void apply(ResponseCreateNewKeyGroup response) {
                    ownKeys = ownKeys.setGroupId(response.getKeyGroupId());
                    encryptionKeysStorage.addOrUpdateItem(0, ownKeys.toByteArray());
                    onMainKeysReady();
                }
            }).failure(new Consumer<Exception>() {
                @Override
                public void apply(Exception e) {
                    Log.w(TAG, "Keys upload error");
                    Log.e(TAG, e);

                    // Just ignore
                }
            }).done(self());
        } else {
            onMainKeysReady();
        }
    }

    private void onMainKeysReady() {
        Log.d(TAG, "Main Keys are ready");

        // Generation of missed ephemeral keys
        int missingKeysCount = Math.max(0, Configuration.EPHEMERAL_KEYS_COUNT - ownKeys.getPreKeys().length);
        if (missingKeysCount > 0) {
            PrivateKey[] nKeys = new PrivateKey[missingKeysCount];
            for (int i = 0; i < missingKeysCount; i++) {
                nKeys[i] = new PrivateKey(
                        RandomUtils.nextRid(),
                        "curve25519",
                        Curve25519.keyGenPrivate(Crypto.randomBytes(64)),
                        false);
            }
            ownKeys = ownKeys.appendPreKeys(nKeys);
            encryptionKeysStorage.addOrUpdateItem(0, ownKeys.toByteArray());
        }


        // Uploading ephemeral keys
        // records = ephemeralStorage.loadAllItems();

        final ArrayList<PrivateKey> pendingEphermalKeys = new ArrayList<PrivateKey>();
        for (PrivateKey key : ownKeys.getPreKeys()) {
            if (!key.isUploaded()) {
                pendingEphermalKeys.add(key);
            }
        }

        if (pendingEphermalKeys.size() > 0) {
            final ArrayList<ApiEncryptionKey> uploadingKeys = new ArrayList<ApiEncryptionKey>();
            ArrayList<ApiEncryptionKeySignature> uploadingSignatures = new ArrayList<ApiEncryptionKeySignature>();
            for (PrivateKey k : pendingEphermalKeys) {
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

            api(new RequestUploadEphermalKey(ownKeys.getKeyGroupId(), uploadingKeys, uploadingSignatures))
                    .then(new Consumer<ResponseVoid>() {
                        @Override
                        public void apply(ResponseVoid responseVoid) {
                            ownKeys = ownKeys.markAsUploaded(pendingEphermalKeys.toArray(new PrivateKey[pendingEphermalKeys.size()]));
                            encryptionKeysStorage.addOrUpdateItem(0, ownKeys.toByteArray());
                            onAllKeysReady();
                        }
                    })
                    .failure(new Consumer<Exception>() {
                        @Override
                        public void apply(Exception e) {
                            Log.w(TAG, "Ephemeral keys upload error");
                            Log.e(TAG, e);

                            // Ignore. This will freeze all encryption operations.
                        }
                    }).done(self());
        } else {
            onAllKeysReady();
        }
    }

    private void onAllKeysReady() {
        Log.d(TAG, "Key Manager started");
        // Now we can start receiving or sending encrypted messages
        isReady = true;
        unstashAll();
    }

    private void fetchOwnKey(PromiseResolver future) {
        Log.d(TAG, "fetchOwnKey");
        future.result(new FetchOwnKeyResult(ownKeys.getIdentityKey()));
    }

    private void fetchKeyGroup(PromiseResolver future) {
        Log.d(TAG, "fetchKeyGroup");
        future.result(new FetchOwnKeyGroupResult(ownKeys.getKeyGroupId()));
    }

    private void fetchEphemeralKey(byte[] publicKey, PromiseResolver future) {
        for (PrivateKey k : ownKeys.getPreKeys()) {
            if (ByteStrings.isEquals(Curve25519.keyGenPublic(k.getKey()), publicKey)) {
                future.result(new FetchEphemeralPrivateKeyRes(k.getKey()));
                return;
            }
        }
        future.error(new RuntimeException("Unable to find ephemeral key"));
    }

    private void fetchEphemeralKey(long keyId, PromiseResolver future) {
        Log.d(TAG, "fetchEphemeralKey: " + keyId);
        for (PrivateKey k : ownKeys.getPreKeys()) {
            if (k.getKeyId() == keyId) {
                future.result(new FetchEphemeralPrivateKeyRes(k.getKey()));
                return;
            }
        }
        future.error(new RuntimeException("Unable to find ephemeral key"));
    }

    private void fetchOwnEphemeralKey(PromiseResolver future) {
        Log.d(TAG, "fetchOwnEphemeralKey");
        PrivateKey ownEphemeralKey = ownKeys.pickRandomPreKey();
        future.result(new FetchOwnEphemeralKeyResult(ownEphemeralKey.getKeyId(),
                ownEphemeralKey.getKey()));
    }

    private void fetchUserGroups(final int uid, final PromiseResolver future) {
        Log.d(TAG, "fetchUserGroups");
        final UserKeys userKeys = getCachedUserKeys(uid);
        if (userKeys != null) {
            Log.d(TAG, "fetchUserGroups:cached");
            future.result(new FetchUserKeyGroupsResponse(userKeys));
            return;
        }
        Log.d(TAG, "fetchUserGroups:loading");
        User user = users().getValue(uid);
        request(new RequestLoadPublicKeyGroups(new ApiUserOutPeer(uid, user.getAccessHash())), new RpcCallback<ResponsePublicKeyGroups>() {
            @Override
            public void onResult(ResponsePublicKeyGroups response) {
                ArrayList<UserKeysGroup> keysGroups = new ArrayList<UserKeysGroup>();
                for (ApiEncryptionKeyGroup keyGroup : response.getPublicKeyGroups()) {
                    UserKeysGroup validatedKeysGroup = validateUserKeysGroup(uid, keyGroup);
                    if (validatedKeysGroup != null) {
                        keysGroups.add(validatedKeysGroup);
                    }
                }
                if (keysGroups.size() != 0) {
                    UserKeys userKeys1 = new UserKeys(uid, keysGroups.toArray(new UserKeysGroup[keysGroups.size()]));
                    cacheUserKeys(userKeys1);
                    future.result(new FetchUserKeyGroupsResponse(userKeys1));
                } else {
                    Log.w(TAG, "(uid:" + uid + ") No valid key groups found");
                    future.error(new RuntimeException("No key groups found"));
                }
            }

            @Override
            public void onError(RpcException e) {
                future.error(e);
            }
        });
    }

    private void fetchUserEphemeralKey(final int uid, final int keyGroupId, final long keyId, final PromiseResolver future) {

        //
        // Searching for group
        //
        final UserKeys keys = getCachedUserKeys(uid);
        UserKeysGroup keysGroup = null;
        for (UserKeysGroup g : keys.getUserKeysGroups()) {
            Log.d(TAG, "KeyGroup (uid: " + uid + "): " + g.getKeyGroupId());
            if (g.getKeyGroupId() == keyGroupId) {
                keysGroup = g;
            }
        }
        if (keysGroup == null) {
            future.error(new RuntimeException("Key Group #" + keyGroupId + " not found"));
            return;
        }

        //
        // Searching in cache
        //
        for (PublicKey p : keysGroup.getEphemeralKeys()) {
            if (p.getKeyId() == keyId) {
                future.result(new FetchUserEphemeralKeyResponse(p));
                return;
            }
        }

        //
        // Downloading ephemeral key
        //

        ArrayList<Long> ids = new ArrayList<Long>();
        ids.add(keyId);
        final UserKeysGroup finalKeysGroup = keysGroup;
        request(new RequestLoadPublicKey(new ApiUserOutPeer(uid, getUser(uid).getAccessHash()), keyGroupId, ids), new RpcCallback<ResponsePublicKeys>() {
            @Override
            public void onResult(ResponsePublicKeys response) {
                if (response.getPublicKey().size() == 0) {
                    Log.w(TAG, "Public key error");
                    future.error(new RuntimeException());
                    return;
                }
                ApiEncryptionKey key = response.getPublicKey().get(0);

                // TODO: Verify signature

                PublicKey pkey = new PublicKey(keyId, key.getKeyAlg(), key.getKeyMaterial());
                UserKeysGroup userKeysGroup = finalKeysGroup.addUserKeyGroup(pkey);
                cacheUserKeys(keys.removeUserKeyGroup(userKeysGroup.getKeyGroupId())
                        .addUserKeyGroup(userKeysGroup));

                future.result(new FetchUserEphemeralKeyResponse(pkey));
            }

            @Override
            public void onError(RpcException e) {
                Log.w(TAG, "Public key error");
                Log.e(TAG, e);
                future.error(e);
            }
        });
    }

    private void fetchUserEphemeralKey(final int uid, int keyGroupId, final PromiseResolver future) {
        request(new RequestLoadEphermalPublicKeys(new ApiUserOutPeer(uid, getUser(uid).getAccessHash()), keyGroupId), new RpcCallback<ResponsePublicKeys>() {
            @Override
            public void onResult(ResponsePublicKeys response) {
                if (response.getPublicKey().size() == 0) {
                    Log.w(TAG, "Public key error");
                    future.error(new RuntimeException());
                    return;
                }
                ApiEncryptionKey key = response.getPublicKey().get(0);

                // TODO: Verify signature

                PublicKey pkey = new PublicKey(key.getKeyId(), key.getKeyAlg(), key.getKeyMaterial());
                // Do not store all ephemeral key as it is not required
                future.result(new FetchUserEphemeralKeyResponse(pkey));
            }

            @Override
            public void onError(RpcException e) {
                Log.w(TAG, "Public key error");
                Log.e(TAG, e);
                future.error(e);
            }
        });
    }

    private void onPublicKeysGroupAdded(int uid, ApiEncryptionKeyGroup keyGroup) {
        UserKeys userKeys = getCachedUserKeys(uid);
        if (userKeys == null) {
            return;
        }
        UserKeysGroup validatedKeysGroup = validateUserKeysGroup(uid, keyGroup);
        if (validatedKeysGroup != null) {
            cacheUserKeys(userKeys.addUserKeyGroup(validatedKeysGroup));
        }
    }

    private void onPublicKeysGroupRemoved(int uid, int keyGroupId) {
        UserKeys userKeys = getCachedUserKeys(uid);
        if (userKeys == null) {
            return;
        }
        cacheUserKeys(userKeys.removeUserKeyGroup(keyGroupId));
    }

    private UserKeysGroup validateUserKeysGroup(int uid, ApiEncryptionKeyGroup keyGroup) {
        if (!"curve25519".equals(keyGroup.getIdentityKey().getKeyAlg())) {
            // Anything other than curve25519 is not supported
            Log.w(TAG, "(uid:" + uid + ") Unsupported identity key alg " + keyGroup.getIdentityKey().getKeyAlg());
            return null;
        }

        PublicKey identity = new PublicKey(
                keyGroup.getIdentityKey().getKeyId(),
                keyGroup.getIdentityKey().getKeyAlg(),
                keyGroup.getIdentityKey().getKeyMaterial());

        ArrayList<PublicKey> keys = new ArrayList<PublicKey>();

        key_loop:
        for (ApiEncryptionKey key : keyGroup.getKeys()) {

            //
            // Validating signatures
            //
            for (ApiEncryptionKeySignature sig : keyGroup.getSignatures()) {
                if (!sig.getSignatureAlg().equals("Ed25519")) {
                    // Anything other than Ed25519 is not supported
                    Log.w(TAG, "(uid:" + uid + ") Unsupported signature algorithm " + sig.getSignatureAlg());
                    continue;
                }
                if (sig.getKeyId() != key.getKeyId()) {
                    continue;
                }

                byte[] keyForSign = RatchetKeySignature.hashForSignature(
                        key.getKeyId(),
                        key.getKeyAlg(),
                        key.getKeyMaterial());

                if (!Curve25519.verifySignature(identity.getPublicKey(), keyForSign, sig.getSignature())) {
                    Log.w(TAG, "(uid:" + uid + ") Unable to verify signature for " + Crypto.keyHash(key.getKeyMaterial()) + " key");
                    continue key_loop;
                }
            }

            //
            // Adding key to collection
            //

            keys.add(new PublicKey(
                    key.getKeyId(),
                    key.getKeyAlg(),
                    key.getKeyMaterial()));
        }

        if (keys.size() > 0) {
            return new UserKeysGroup(keyGroup.getKeyGroupId(), identity, keys.toArray(new PublicKey[keys.size()]),
                    new PublicKey[0]);
        } else {
            Log.w(TAG, "(uid:" + uid + ") No valid keys in key group #" + keyGroup.getKeyGroupId());
            return null;
        }
    }

    private UserKeys getCachedUserKeys(int uid) {
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
        return userKeys;
    }

    private void cacheUserKeys(UserKeys userKeys) {
        encryptionKeysStorage.addOrUpdateItem(userKeys.getUid(), userKeys.toByteArray());
        cachedUserKeys.put(userKeys.getUid(), userKeys);
    }

    @Override
    public void onReceive(Object message) {
        if (!isReady
                && (message instanceof AskIntRequest
                || message instanceof PublicKeysGroupAdded
                || message instanceof PublicKeysGroupRemoved)) {
            stash();
            return;
        }
        if (message instanceof PublicKeysGroupAdded) {
            PublicKeysGroupAdded publicKeysGroupAdded = (PublicKeysGroupAdded) message;
            onPublicKeysGroupAdded(publicKeysGroupAdded.getUid(), publicKeysGroupAdded.getPublicKeyGroup());
        } else if (message instanceof PublicKeysGroupRemoved) {
            PublicKeysGroupRemoved publicKeysGroupRemoved = (PublicKeysGroupRemoved) message;
            onPublicKeysGroupRemoved(publicKeysGroupRemoved.getUid(), publicKeysGroupRemoved.getKeyGroupId());
        } else {
            super.onReceive(message);
        }
    }

    @Override
    public void onAsk(Object message, PromiseResolver future) {
        if (message instanceof FetchOwnKey) {
            fetchOwnKey(future);
        } else if (message instanceof FetchOwnKeyGroup) {
            fetchKeyGroup(future);
        } else if (message instanceof FetchEphemeralPrivateKey) {
            fetchEphemeralKey(((FetchEphemeralPrivateKey) message).getPublicKey(), future);
        } else if (message instanceof FetchEphemeralPrivateKeyById) {
            fetchEphemeralKey(((FetchEphemeralPrivateKeyById) message).getKeyId(), future);
        } else if (message instanceof FetchUserKeyGroups) {
            fetchUserGroups(((FetchUserKeyGroups) message).getUid(), future);
        } else if (message instanceof FetchUserEphemeralKey) {
            fetchUserEphemeralKey(((FetchUserEphemeralKey) message).getUid(), ((FetchUserEphemeralKey) message).getKeyGroup(), ((FetchUserEphemeralKey) message).getKeyId(), future);
        } else if (message instanceof FetchUserEphemeralKeyRandom) {
            fetchUserEphemeralKey(((FetchUserEphemeralKeyRandom) message).getUid(), ((FetchUserEphemeralKeyRandom) message).getKeyGroup(), future);
        } else if (message instanceof FetchOwnEphemeralKey) {
            fetchOwnEphemeralKey(future);
        } else {
            super.onAsk(message, future);
        }
    }

    public static class FetchOwnKey extends AskMessage<FetchOwnKeyResult> {

    }

    public static class FetchOwnKeyResult extends AskResult {

        private PrivateKey identityKey;

        public FetchOwnKeyResult(PrivateKey identityKey) {
            this.identityKey = identityKey;
        }

        public PrivateKey getIdentityKey() {
            return identityKey;
        }
    }

    public static class FetchOwnEphemeralKey extends AskMessage<FetchOwnEphemeralKeyResult> {

    }

    public static class FetchOwnEphemeralKeyResult extends AskResult {

        private long id;
        private byte[] privateKey;

        public FetchOwnEphemeralKeyResult(long id, byte[] privateKey) {
            this.id = id;
            this.privateKey = privateKey;
        }

        public long getId() {
            return id;
        }

        public byte[] getPrivateKey() {
            return privateKey;
        }
    }

    public static class FetchOwnKeyGroup extends AskMessage<FetchOwnKeyGroupResult> {

    }

    public static class FetchOwnKeyGroupResult extends AskResult {
        private int keyGroupId;

        public FetchOwnKeyGroupResult(int keyGroupId) {
            this.keyGroupId = keyGroupId;
        }

        public int getKeyGroupId() {
            return keyGroupId;
        }
    }

    public static class FetchEphemeralPrivateKey extends AskMessage<FetchEphemeralPrivateKeyRes> {

        private byte[] publicKey;

        public FetchEphemeralPrivateKey(byte[] publicKey) {
            this.publicKey = publicKey;
        }

        public byte[] getPublicKey() {
            return publicKey;
        }
    }

    public static class FetchEphemeralPrivateKeyById extends AskMessage<FetchEphemeralPrivateKeyRes> {

        private long keyId;

        public FetchEphemeralPrivateKeyById(long keyId) {
            this.keyId = keyId;
        }

        public long getKeyId() {
            return keyId;
        }
    }

    public static class FetchEphemeralPrivateKeyRes extends AskResult {
        private byte[] privateKey;

        public FetchEphemeralPrivateKeyRes(byte[] privateKey) {
            this.privateKey = privateKey;
        }

        public byte[] getPrivateKey() {
            return privateKey;
        }
    }

    public static class FetchUserKeyGroups extends AskMessage<FetchUserKeyGroupsResponse> {
        private int uid;

        public FetchUserKeyGroups(int uid) {
            this.uid = uid;
        }

        public int getUid() {
            return uid;
        }
    }

    public static class FetchUserKeyGroupsResponse extends AskResult {

        private UserKeys userKeys;

        public FetchUserKeyGroupsResponse(UserKeys userKeys) {
            this.userKeys = userKeys;
        }

        public UserKeys getUserKeys() {
            return userKeys;
        }
    }

    public static class FetchUserEphemeralKey extends AskMessage<FetchUserEphemeralKeyResponse> {

        private int uid;
        private int keyGroup;
        private long keyId;

        public FetchUserEphemeralKey(int uid, int keyGroup, long keyId) {
            this.keyGroup = keyGroup;
            this.uid = uid;
            this.keyId = keyId;
        }

        public int getUid() {
            return uid;
        }

        public long getKeyId() {
            return keyId;
        }

        public int getKeyGroup() {
            return keyGroup;
        }
    }

    public static class FetchUserEphemeralKeyRandom extends AskMessage<FetchUserEphemeralKeyResponse> {

        private int uid;
        private int keyGroup;

        public FetchUserEphemeralKeyRandom(int uid, int keyGroup) {
            this.keyGroup = keyGroup;
            this.uid = uid;
        }

        public int getUid() {
            return uid;
        }

        public int getKeyGroup() {
            return keyGroup;
        }
    }

    public static class FetchUserEphemeralKeyResponse extends AskResult {
        private PublicKey ephemeralKey;

        public FetchUserEphemeralKeyResponse(PublicKey ephemeralKey) {
            this.ephemeralKey = ephemeralKey;
        }

        public PublicKey getEphemeralKey() {
            return ephemeralKey;
        }
    }

    public static class PublicKeysGroupAdded {

        private int uid;
        private ApiEncryptionKeyGroup publicKeyGroup;

        public PublicKeysGroupAdded(int uid, ApiEncryptionKeyGroup publicKeyGroup) {
            this.uid = uid;
            this.publicKeyGroup = publicKeyGroup;
        }

        public int getUid() {
            return uid;
        }

        public ApiEncryptionKeyGroup getPublicKeyGroup() {
            return publicKeyGroup;
        }
    }

    public static class PublicKeysGroupRemoved {
        private int uid;
        private int keyGroupId;

        public PublicKeysGroupRemoved(int uid, int keyGroupId) {
            this.uid = uid;
            this.keyGroupId = keyGroupId;
        }

        public int getUid() {
            return uid;
        }

        public int getKeyGroupId() {
            return keyGroupId;
        }
    }
}