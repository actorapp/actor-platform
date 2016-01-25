package im.actor.core.modules.encryption;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import im.actor.core.api.ApiEncryptionKey;
import im.actor.core.api.ApiEncryptionKeyGroup;
import im.actor.core.api.ApiEncryptionKeySignature;
import im.actor.core.api.ApiEncryptionPublicKeyGroup;
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
import im.actor.runtime.bser.BserWriter;
import im.actor.runtime.bser.DataOutput;
import im.actor.runtime.crypto.Curve25519;
import im.actor.runtime.crypto.primitives.digest.SHA256;
import im.actor.runtime.crypto.primitives.util.ByteStrings;
import im.actor.runtime.crypto.ratchet.RatchetKeySignature;
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
        Log.d(TAG, "All Keys are ready");
        // Now we can start receiving or sending encrypted messages
        isReady = true;
        unstashAll();
    }

    private void fetchOwnKey(Future future) {
        Log.d(TAG, "fetchOwnKey");
        future.onResult(new FetchOwnKeyResult(ownKeys.getIdentityKey()));
    }

    private void fetchKeyGroup(Future future) {
        Log.d(TAG, "fetchKeyGroup");
        future.onResult(new FetchOwnKeyGroupResult(ownKeys.getKeyGroupId()));
    }

    private void fetchEphemeralKey(byte[] publicKey, Future future) {
        for (OwnPrivateKey k : ownKeys.getEphemeralKeys()) {
            if (ByteStrings.isEquals(Curve25519.keyGenPublic(k.getKey()), publicKey)) {
                future.onResult(new FetchEphemeralPrivateKeyRes(k.getKey()));
                return;
            }
        }
        future.onError(new RuntimeException("Unable to find ephemeral key"));
    }

    private void fetchEphemeralKey(long keyId, Future future) {
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
        final UserKeys userKeys = getCachedUserKeys(uid);
        if (userKeys != null) {
            Log.d(TAG, "fetchUserGroups:cached");
            future.onResult(new FetchUserKeyGroupsResponse(userKeys));
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
                    future.onResult(new FetchUserKeyGroupsResponse(userKeys1));
                } else {
                    Log.w(TAG, "(uid:" + uid + ") No valid key groups found");
                    future.onError(new RuntimeException("No key groups found"));
                }
            }

            @Override
            public void onError(RpcException e) {
                future.onError(e);
            }
        });
    }

    private void fetchUserEphemeralKey(final int uid, final int keyGroupId, final long keyId, final Future future) {

        //
        // Searching for group
        //
        final UserKeys keys = getCachedUserKeys(uid);
        UserKeysGroup keysGroup = null;
        for (UserKeysGroup g : keys.getUserKeysGroups()) {
            if (g.getKeyGroupId() == keyGroupId) {
                keysGroup = g;
            }
        }
        if (keysGroup == null) {
            future.onError(new RuntimeException("Key Group not found"));
            return;
        }

        //
        // Searching in cache
        //
        for (UserPublicKey p : keysGroup.getEphemeralKeys()) {
            if (p.getKeyId() == keyId) {
                future.onResult(new FetchUserEphemeralKeyResponse(p));
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
                    future.onError(new RuntimeException());
                    return;
                }
                ApiEncryptionKey key = response.getPublicKey().get(0);

                // TODO: Verify signature

                UserPublicKey pkey = new UserPublicKey(keyId, key.getKeyAlg(), key.getKeyMaterial());
                UserKeysGroup userKeysGroup = finalKeysGroup.addUserKeyGroup(pkey);
                cacheUserKeys(keys.removeUserKeyGroup(userKeysGroup.getKeyGroupId())
                        .addUserKeyGroup(userKeysGroup));

                future.onResult(new FetchUserEphemeralKeyResponse(pkey));
            }

            @Override
            public void onError(RpcException e) {
                Log.w(TAG, "Public key error");
                Log.e(TAG, e);
                future.onError(e);
            }
        });

        //            request(new RequestLoadEphermalPublicKeys(new ApiUserOutPeer(uid, getUser(uid).getAccessHash()), encryptionKeyGroup.getKeyGroupId()), new RpcCallback<ResponsePublicKeys>() {
//                @Override
//                public void onResult(ResponsePublicKeys response) {
//                    if (response.getPublicKey().size() == 0) {
//                        Log.w(TAG, "No ephemeral keys found");
//                        return;
//                    }
//
//                    ApiEncryptionKey encryptionKey = response.getPublicKey().get(RandomUtils.randomId(response.getPublicKey().size()));
//                    theirEphermalKey0 = new UserPublicKey(encryptionKey.getKeyId(), encryptionKey.getKeyAlg(), encryptionKey.getKeyMaterial());
//                    onTheirReady0();
//                }
//
//                @Override
//                public void onError(RpcException e) {
//                    // Nothing to do
//                    Log.w(TAG, "Their ephemeral error");
//                    Log.e(TAG, e);
//                }
//            });
    }

    private void fetchUserEphemeralKey(final int uid, int keyGroupId, final Future future) {
        request(new RequestLoadEphermalPublicKeys(new ApiUserOutPeer(uid, getUser(uid).getAccessHash()), keyGroupId), new RpcCallback<ResponsePublicKeys>() {
            @Override
            public void onResult(ResponsePublicKeys response) {
                if (response.getPublicKey().size() == 0) {
                    Log.w(TAG, "Public key error");
                    future.onError(new RuntimeException());
                    return;
                }
                ApiEncryptionKey key = response.getPublicKey().get(0);

                // TODO: Verify signature

                UserPublicKey pkey = new UserPublicKey(key.getKeyId(), key.getKeyAlg(), key.getKeyMaterial());
//                UserKeysGroup userKeysGroup = finalKeysGroup.addUserKeyGroup(pkey);
//                cacheUserKeys(keys.removeUserKeyGroup(userKeysGroup.getKeyGroupId())
//                        .addUserKeyGroup(userKeysGroup));

                future.onResult(new FetchUserEphemeralKeyResponse(pkey));
            }

            @Override
            public void onError(RpcException e) {
                Log.w(TAG, "Public key error");
                Log.e(TAG, e);
                future.onError(e);
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

        UserPublicKey identity = new UserPublicKey(
                keyGroup.getIdentityKey().getKeyId(),
                keyGroup.getIdentityKey().getKeyAlg(),
                keyGroup.getIdentityKey().getKeyMaterial());

        ArrayList<UserPublicKey> keys = new ArrayList<UserPublicKey>();

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

            keys.add(new UserPublicKey(
                    key.getKeyId(),
                    key.getKeyAlg(),
                    key.getKeyMaterial()));
        }

        if (keys.size() > 0) {
            return new UserKeysGroup(keyGroup.getKeyGroupId(), identity, keys.toArray(new UserPublicKey[keys.size()]),
                    new UserPublicKey[0]);
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
                && (message instanceof AskRequest
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
        } else if (message instanceof FetchUserEphemeralKey) {
            fetchUserEphemeralKey(((FetchUserEphemeralKey) message).getUid(), ((FetchUserEphemeralKey) message).getKeyGroup(), ((FetchUserEphemeralKey) message).getKeyId(), future);
            return false;
        } else if (message instanceof FetchUserEphemeralKeyRandom) {
            fetchUserEphemeralKey(((FetchUserEphemeralKeyRandom) message).getUid(), ((FetchUserEphemeralKeyRandom) message).getKeyGroup(), future);
            return false;
        }
        return super.onAsk(message, future);
    }

    public static class FetchOwnKey {

    }

    public static class FetchOwnKeyResult {

        private OwnPrivateKey identityKey;

        public FetchOwnKeyResult(OwnPrivateKey identityKey) {
            this.identityKey = identityKey;
        }

        public OwnPrivateKey getIdentityKey() {
            return identityKey;
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

    public static class FetchUserEphemeralKey {

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

    public static class FetchUserEphemeralKeyRandom {

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

    public static class FetchUserEphemeralKeyResponse {
        private UserPublicKey ephemeralKey;

        public FetchUserEphemeralKeyResponse(UserPublicKey ephemeralKey) {
            this.ephemeralKey = ephemeralKey;
        }

        public UserPublicKey getEphemeralKey() {
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