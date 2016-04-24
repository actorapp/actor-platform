package im.actor.core.modules.encryption;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import im.actor.core.api.ApiEncryptionKey;
import im.actor.core.api.ApiEncryptionKeyGroup;
import im.actor.core.api.ApiEncryptionKeySignature;
import im.actor.core.api.ApiUserOutPeer;
import im.actor.core.api.rpc.RequestCreateNewKeyGroup;
import im.actor.core.api.rpc.RequestLoadPrePublicKeys;
import im.actor.core.api.rpc.RequestLoadPublicKey;
import im.actor.core.api.rpc.RequestLoadPublicKeyGroups;
import im.actor.core.api.rpc.RequestUploadPreKey;
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
import im.actor.core.modules.ModuleActor;
import im.actor.core.util.RandomUtils;
import im.actor.runtime.Crypto;
import im.actor.runtime.Log;
import im.actor.runtime.Storage;
import im.actor.runtime.actors.ask.AskIntRequest;
import im.actor.runtime.actors.ask.AskMessage;
import im.actor.runtime.actors.ask.AskResult;
import im.actor.runtime.collections.ManagedList;
import im.actor.runtime.crypto.Curve25519KeyPair;
import im.actor.runtime.function.Function;
import im.actor.runtime.promise.Promise;
import im.actor.runtime.crypto.Curve25519;
import im.actor.runtime.crypto.ratchet.RatchetKeySignature;
import im.actor.runtime.function.Consumer;
import im.actor.runtime.promise.PromisesArray;
import im.actor.runtime.function.Tuple2;
import im.actor.runtime.storage.KeyValueStorage;

/**
 * Key Management Actor.
 * 1) Generates and uploads own keys.
 * 2) Downloads and manages updates about foreign keys
 */
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

        //
        // Initialization key storage
        //

        encryptionKeysStorage = Storage.createKeyValue("encryption_keys");

        //
        // Initialization own private keys
        //

        ownKeys = null;
        byte[] ownKeysStorage = encryptionKeysStorage.loadItem(0);
        if (ownKeysStorage != null) {
            try {
                ownKeys = new PrivateKeyStorage(ownKeysStorage);

                // If we need re-save key storage
                if (ownKeys.isWasRegenerated()) {
                    encryptionKeysStorage.addOrUpdateItem(0, ownKeys.toByteArray());
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        if (ownKeys == null) {
            Curve25519KeyPair identityPrivate = Curve25519.keyGen(Crypto.randomBytes(64));
            Curve25519KeyPair key0 = Curve25519.keyGen(Crypto.randomBytes(64));

            ownKeys = new PrivateKeyStorage(0,
                    new PrivateKey(RandomUtils.nextRid(), "curve25519", identityPrivate.getPrivateKey(),
                            identityPrivate.getPublicKey()),
                    new PrivateKey[]{
                            new PrivateKey(RandomUtils.nextRid(), "curve25519", key0.getPrivateKey(),
                                    key0.getPublicKey())
                    },
                    new PrivateKey[0]);
            encryptionKeysStorage.addOrUpdateItem(0, ownKeys.toByteArray());
        }

        //
        // Creating new key group if needed
        //

        if (ownKeys.getKeyGroupId() == 0) {

            ApiEncryptionKey identityKey = ownKeys.getIdentityKey().toApiKey();

            ArrayList<ApiEncryptionKey> keys = ManagedList.of(ownKeys.getKeys())
                    .map(PrivateKey.TO_API);
            ArrayList<ApiEncryptionKeySignature> signatures = ManagedList.of(ownKeys.getKeys())
                    .map(PrivateKey.SIGN(ownKeys.getIdentityKey()));

            Log.d(TAG, "Creation of new key group");
            api(new RequestCreateNewKeyGroup(identityKey, Configuration.SUPPORTED, keys, signatures)).then(new Consumer<ResponseCreateNewKeyGroup>() {
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
            });
        } else {
            onMainKeysReady();
        }
    }

    private void onMainKeysReady() {
        Log.d(TAG, "Main Keys are ready");

        //
        // Generation required pre keys
        //

        int missingKeysCount = Math.max(0, Configuration.EPHEMERAL_KEYS_COUNT - ownKeys.getPreKeys().length);
        if (missingKeysCount > 0) {
            ownKeys = ownKeys.appendPreKeys(
                    ManagedList.of(PrivateKey.GENERATOR, missingKeysCount)
                            .toArray(new PrivateKey[0]));
            encryptionKeysStorage.addOrUpdateItem(0, ownKeys.toByteArray());
        }

        //
        // Uploading own pre keys
        //

        final ManagedList<PrivateKey> pendingEphermalKeys =
                ManagedList.of(ownKeys.getPreKeys())
                        .filter(PrivateKey.NOT_UPLOADED);

        if (pendingEphermalKeys.size() > 0) {

            ArrayList<ApiEncryptionKey> uploadingKeys =
                    pendingEphermalKeys.map(PrivateKey.TO_API);
            ArrayList<ApiEncryptionKeySignature> uploadingSignatures =
                    pendingEphermalKeys.map(PrivateKey.SIGN(ownKeys.getIdentityKey()));

            api(new RequestUploadPreKey(ownKeys.getKeyGroupId(), uploadingKeys, uploadingSignatures))
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
                    });
        } else {
            onAllKeysReady();
        }
    }

    private void onAllKeysReady() {

        //
        // Finished starting key manager
        //

        Log.d(TAG, "Key Manager started with key group #" + ownKeys.getKeyGroupId());
        isReady = true;
        unstashAll();
    }

    //
    // Own Keys fetching
    //

    /**
     * Fetching Own Identity key and group id
     */
    private Promise<OwnIdentity> fetchOwnIdentity() {
        return Promise.success(new OwnIdentity(ownKeys.getKeyGroupId(), ownKeys.getIdentityKey()));
    }

    /**
     * Fetching own private pre key by public key
     *
     * @param publicKey public key material for search
     */
    private Promise<PrivateKey> fetchPreKey(byte[] publicKey) {
        try {
            return Promise.success(ManagedList.of(ownKeys.getPreKeys())
                    .filter(PrivateKey.PRE_KEY_EQUALS(publicKey))
                    .first());
        } catch (Exception e) {
            Log.d(TAG, "Unable to find own pre key " + Crypto.keyHash(publicKey));
            for (PrivateKey p : ownKeys.getPreKeys()) {
                Log.d(TAG, "Have: " + Crypto.keyHash(p.getPublicKey()));
            }
            throw e;
        }
    }

    /**
     * Fetching own pre key by id
     *
     * @param keyId pre key id
     */
    private Promise<PrivateKey> fetchPreKey(long keyId) {
        try {
            return Promise.success(ManagedList.of(ownKeys.getPreKeys())
                    .filter(PrivateKey.PRE_KEY_EQUALS_ID(keyId))
                    .first());
        } catch (Exception e) {
            Log.d(TAG, "Unable to find own pre key #" + keyId);
            throw e;
        }
    }

    /**
     * Fetching own random pre key
     */
    private Promise<PrivateKey> fetchPreKey() {
        return PromisesArray.of(ownKeys.getPreKeys())
                .random();
    }

    //
    // User keys fetching
    //

    /**
     * Fetching all user's key groups
     *
     * @param uid User's id
     */
    private Promise<UserKeys> fetchUserGroups(final int uid) {

        User user = users().getValue(uid);
        if (user == null) {
            throw new RuntimeException("Unable to find user #" + uid);
        }

        final UserKeys userKeys = getCachedUserKeys(uid);
        if (userKeys != null) {
            return Promise.success(userKeys);
        }

        return api(new RequestLoadPublicKeyGroups(new ApiUserOutPeer(uid, user.getAccessHash())))
                .map(new Function<ResponsePublicKeyGroups, ArrayList<UserKeysGroup>>() {
                    @Override
                    public ArrayList<UserKeysGroup> apply(ResponsePublicKeyGroups response) {
                        ArrayList<UserKeysGroup> keysGroups = new ArrayList<>();
                        for (ApiEncryptionKeyGroup keyGroup : response.getPublicKeyGroups()) {
                            UserKeysGroup validatedKeysGroup = validateUserKeysGroup(uid, keyGroup);
                            if (validatedKeysGroup != null) {
                                keysGroups.add(validatedKeysGroup);
                            }
                        }
                        return keysGroups;
                    }
                })
                .map(new Function<ArrayList<UserKeysGroup>, UserKeys>() {
                    @Override
                    public UserKeys apply(ArrayList<UserKeysGroup> userKeysGroups) {
                        UserKeys userKeys = new UserKeys(uid, userKeysGroups.toArray(new UserKeysGroup[userKeysGroups.size()]));
                        cacheUserKeys(userKeys);
                        return userKeys;
                    }
                });
    }

    /**
     * Fetching user's pre key by key id
     *
     * @param uid        User's id
     * @param keyGroupId User's key group id
     * @param keyId      Key id
     */
    private Promise<PublicKey> fetchUserPreKey(final int uid, final int keyGroupId, final long keyId) {

        User user = users().getValue(uid);
        if (user == null) {
            throw new RuntimeException("Unable to find user #" + uid);
        }

        return pickUserGroup(uid, keyGroupId)
                .flatMap(new Function<Tuple2<UserKeysGroup, UserKeys>, Promise<PublicKey>>() {
                    @Override
                    public Promise<PublicKey> apply(final Tuple2<UserKeysGroup, UserKeys> keysGroup) {

                        //
                        // Searching in cache
                        //

                        for (PublicKey p : keysGroup.getT1().getEphemeralKeys()) {
                            if (p.getKeyId() == keyId) {
                                return Promise.success(p);
                            }
                        }

                        //
                        // Downloading pre key
                        //

                        ArrayList<Long> ids = new ArrayList<Long>();
                        ids.add(keyId);
                        final UserKeysGroup finalKeysGroup = keysGroup.getT1();

                        return api(new RequestLoadPublicKey(new ApiUserOutPeer(uid, getUser(uid).getAccessHash()), keyGroupId, ids))
                                .map(new Function<ResponsePublicKeys, PublicKey>() {
                                    @Override
                                    public PublicKey apply(ResponsePublicKeys responsePublicKeys) {
                                        if (responsePublicKeys.getPublicKey().size() == 0) {
                                            throw new RuntimeException("Unable to find public key on server");
                                        }
                                        ApiEncryptionKeySignature sig = null;
                                        for (ApiEncryptionKeySignature s : responsePublicKeys.getSignatures()) {
                                            if (s.getKeyId() == keyId && "Ed25519".equals(s.getSignatureAlg())) {
                                                sig = s;
                                                break;
                                            }
                                        }
                                        if (sig == null) {
                                            throw new RuntimeException("Unable to find public key on server");
                                        }

                                        ApiEncryptionKey key = responsePublicKeys.getPublicKey().get(0);

                                        byte[] keyHash = RatchetKeySignature.hashForSignature(key.getKeyId(), key.getKeyAlg(),
                                                key.getKeyMaterial());

                                        if (!Curve25519.verifySignature(keysGroup.getT1().getIdentityKey().getPublicKey(),
                                                keyHash, sig.getSignature())) {
                                            throw new RuntimeException("Key signature does not match");
                                        }

                                        PublicKey pkey = new PublicKey(keyId, key.getKeyAlg(), key.getKeyMaterial());
                                        UserKeysGroup userKeysGroup = finalKeysGroup.addPublicKey(pkey);
                                        cacheUserKeys(keysGroup.getT2().removeUserKeyGroup(userKeysGroup.getKeyGroupId())
                                                .addUserKeyGroup(userKeysGroup));

                                        return pkey;
                                    }
                                });
                    }
                });
    }

    /**
     * Fetching user's random pre key
     *
     * @param uid        User's id
     * @param keyGroupId User's key group id
     */
    private Promise<PublicKey> fetchUserPreKey(final int uid, final int keyGroupId) {
        return pickUserGroup(uid, keyGroupId)
                .flatMap(new Function<Tuple2<UserKeysGroup, UserKeys>, Promise<PublicKey>>() {
                    @Override
                    public Promise<PublicKey> apply(final Tuple2<UserKeysGroup, UserKeys> keyGroups) {
                        return api(new RequestLoadPrePublicKeys(new ApiUserOutPeer(uid, getUser(uid).getAccessHash()), keyGroupId))
                                .map(new Function<ResponsePublicKeys, PublicKey>() {
                                    @Override
                                    public PublicKey apply(ResponsePublicKeys response) {
                                        if (response.getPublicKey().size() == 0) {
                                            throw new RuntimeException("User doesn't have pre keys");
                                        }
                                        ApiEncryptionKey key = response.getPublicKey().get(0);
                                        ApiEncryptionKeySignature sig = null;
                                        for (ApiEncryptionKeySignature s : response.getSignatures()) {
                                            if (s.getKeyId() == key.getKeyId() && "Ed25519".equals(s.getSignatureAlg())) {
                                                sig = s;
                                                break;
                                            }
                                        }
                                        if (sig == null) {
                                            throw new RuntimeException("Unable to find public key on server");
                                        }

                                        byte[] keyHash = RatchetKeySignature.hashForSignature(key.getKeyId(), key.getKeyAlg(),
                                                key.getKeyMaterial());

                                        if (!Curve25519.verifySignature(keyGroups.getT1().getIdentityKey().getPublicKey(),
                                                keyHash, sig.getSignature())) {
                                            throw new RuntimeException("Key signature does not match");
                                        }

                                        return new PublicKey(key.getKeyId(), key.getKeyAlg(), key.getKeyMaterial());
                                    }
                                });
                    }
                });
    }

    //
    // Keys updates handling
    //

    /**
     * Handler for adding new key group
     *
     * @param uid      User's id
     * @param keyGroup Added key group
     */
    private void onPublicKeysGroupAdded(int uid, ApiEncryptionKeyGroup keyGroup) {
        UserKeys userKeys = getCachedUserKeys(uid);
        if (userKeys == null) {
            return;
        }
        UserKeysGroup validatedKeysGroup = validateUserKeysGroup(uid, keyGroup);
        if (validatedKeysGroup != null) {
            UserKeys updatedUserKeys = userKeys.addUserKeyGroup(validatedKeysGroup);
            cacheUserKeys(updatedUserKeys);
            context().getEncryption().getEncryptedChatManager(uid)
                    .send(new EncryptedPeerActor.KeyGroupUpdated(userKeys));
        }
    }

    /**
     * Handler for removing key group
     *
     * @param uid        User's id
     * @param keyGroupId Removed key group id
     */
    private void onPublicKeysGroupRemoved(int uid, int keyGroupId) {
        UserKeys userKeys = getCachedUserKeys(uid);
        if (userKeys == null) {
            return;
        }

        UserKeys updatedUserKeys = userKeys.removeUserKeyGroup(keyGroupId);
        cacheUserKeys(updatedUserKeys);
        context().getEncryption().getEncryptedChatManager(uid)
                .send(new EncryptedPeerActor.KeyGroupUpdated(userKeys));
    }

    //
    // Helper methods
    //

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

        if (keys.size() == 0) {
            Log.w(TAG, "(uid:" + uid + ") No valid keys in key group #" + keyGroup.getKeyGroupId());
        }

        return new UserKeysGroup(keyGroup.getKeyGroupId(), identity, keys.toArray(new PublicKey[keys.size()]),
                new PublicKey[0]);
    }

    private Promise<Tuple2<UserKeysGroup, UserKeys>> pickUserGroup(int uid, final int keyGroupId) {
        return fetchUserGroups(uid)
                .map(userKeys -> {
                    UserKeysGroup keysGroup = null;
//                    for (UserKeysGroup g : userKeys.getUserKeysGroups()) {
//                        if (g.getKeyGroupId() == keyGroupId) {
//                            keysGroup = g;
//                        }
//                    }
//                    if (keysGroup == null) {
//                        throw new RuntimeException("Key Group #" + keyGroupId + " not found");
//                    }
//                    return new Tuple2<>(keysGroup, userKeys);
                    return null;
                });
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

    //
    // Messages
    //

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
    public Promise onAsk(Object message) throws Exception {
        if (message instanceof FetchOwnKey) {
            return fetchOwnIdentity();
        } else if (message instanceof FetchOwnPreKeyByPublic) {
            return fetchPreKey(((FetchOwnPreKeyByPublic) message).getPublicKey());
        } else if (message instanceof FetchOwnPreKeyById) {
            return fetchPreKey(((FetchOwnPreKeyById) message).getKeyId());
        } else if (message instanceof FetchUserKeys) {
            return fetchUserGroups(((FetchUserKeys) message).getUid());
        } else if (message instanceof FetchUserPreKey) {
            return fetchUserPreKey(((FetchUserPreKey) message).getUid(), ((FetchUserPreKey) message).getKeyGroup(), ((FetchUserPreKey) message).getKeyId());
        } else if (message instanceof FetchUserPreKeyRandom) {
            return fetchUserPreKey(((FetchUserPreKeyRandom) message).getUid(), ((FetchUserPreKeyRandom) message).getKeyGroup());
        } else if (message instanceof FetchOwnRandomPreKey) {
            return fetchPreKey();
        } else {
            return super.onAsk(message);
        }
    }

    //
    // Own Keys
    //

    public static class FetchOwnKey implements AskMessage<OwnIdentity> {

    }

    public static class OwnIdentity extends AskResult {

        private int keyGroup;
        private PrivateKey identityKey;

        public OwnIdentity(int keyGroup, PrivateKey identityKey) {
            this.keyGroup = keyGroup;
            this.identityKey = identityKey;
        }

        public int getKeyGroup() {
            return keyGroup;
        }

        public PrivateKey getIdentityKey() {
            return identityKey;
        }
    }

    public static class FetchOwnRandomPreKey implements AskMessage<PrivateKey> {

    }

    public static class FetchOwnPreKeyByPublic implements AskMessage<PrivateKey> {

        private byte[] publicKey;

        public FetchOwnPreKeyByPublic(byte[] publicKey) {
            this.publicKey = publicKey;
        }

        public byte[] getPublicKey() {
            return publicKey;
        }
    }

    public static class FetchOwnPreKeyById implements AskMessage<PrivateKey> {

        private long keyId;

        public FetchOwnPreKeyById(long keyId) {
            this.keyId = keyId;
        }

        public long getKeyId() {
            return keyId;
        }
    }

    //
    // Users Keys
    //

    public static class FetchUserKeys implements AskMessage<UserKeys> {
        private int uid;

        public FetchUserKeys(int uid) {
            this.uid = uid;
        }

        public int getUid() {
            return uid;
        }
    }

    public static class FetchUserPreKey implements AskMessage<PublicKey> {

        private int uid;
        private int keyGroup;
        private long keyId;

        public FetchUserPreKey(int uid, int keyGroup, long keyId) {
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

    public static class FetchUserPreKeyRandom implements AskMessage<PublicKey> {

        private int uid;
        private int keyGroup;

        public FetchUserPreKeyRandom(int uid, int keyGroup) {
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

    //
    // Updates handling
    //

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