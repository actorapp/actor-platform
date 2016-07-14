package im.actor.core.modules.encryption.ratchet;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;

import im.actor.core.entity.encryption.PeerSession;
import im.actor.core.entity.encryption.PeerSessionsStorage;
import im.actor.core.modules.ModuleContext;
import im.actor.core.modules.encryption.ratchet.entity.OwnIdentity;
import im.actor.core.modules.encryption.ratchet.entity.PrivateKey;
import im.actor.core.modules.encryption.ratchet.entity.PublicKey;
import im.actor.core.modules.encryption.ratchet.entity.UserKeys;
import im.actor.core.modules.encryption.ratchet.entity.UserKeysGroup;
import im.actor.core.util.BaseKeyValueEngine;
import im.actor.core.modules.ModuleActor;
import im.actor.core.util.RandomUtils;
import im.actor.runtime.Storage;
import im.actor.runtime.actors.ask.AskMessage;
import im.actor.runtime.collections.ManagedList;
import im.actor.runtime.crypto.ratchet.RatchetMasterSecret;
import im.actor.runtime.crypto.ratchet.RatchetPrivateKey;
import im.actor.runtime.crypto.ratchet.RatchetPublicKey;
import im.actor.runtime.promise.Promise;
import im.actor.runtime.promise.Promises;
import im.actor.runtime.storage.KeyValueEngine;

/**
 * Stores and manages encrypted sessions between peers.
 * Can be asked to pick session parameters for specific peer.
 */
public class SessionManagerActor extends ModuleActor {

    private static final String TAG = "SessionManagerActor";

    private KeyValueEngine<PeerSessionsStorage> peerSessions;
    private KeyManager keyManager;
    private final HashSet<Integer> locked = new HashSet<>();

    public SessionManagerActor(ModuleContext context) {
        super(context);
    }

    @Override
    public void preStart() {
        super.preStart();
        keyManager = context().getEncryption().getKeyManager();
        peerSessions = new BaseKeyValueEngine<PeerSessionsStorage>(Storage.createKeyValue("encryption_sessions")) {

            @Override
            protected byte[] serialize(PeerSessionsStorage value) {
                return value.toByteArray();
            }

            @Override
            protected PeerSessionsStorage deserialize(byte[] data) {
                try {
                    return new PeerSessionsStorage(data);
                } catch (IOException e) {
                    e.printStackTrace();
                    return null;
                }
            }
        };
    }

    /**
     * Pick session
     *
     * @param uid        User's id
     * @param keyGroupId User's key group
     */
    public Promise<PeerSession> pickSession(final int uid,
                                            final int keyGroupId) {

        PeerSession cached = pickCachedSession(uid, keyGroupId);
        if (cached != null) {
            return Promise.success(cached);
        }

        if (locked.contains(uid)) {
            stash();
            return null;
        }
        locked.add(uid);

        return Promises.tuple(
                keyManager.getOwnIdentity(),
                keyManager.getOwnRandomPreKey(),
                keyManager.getUserKeyGroups(uid),
                keyManager.getUserRandomPreKey(uid, keyGroupId))
                .map(tuple -> {

                    OwnIdentity ownIdentity = tuple.getT1();
                    PrivateKey ownPreKey = tuple.getT2();
                    UserKeys userKeys = tuple.getT3();
                    PublicKey theirPreKey = tuple.getT4();

                    UserKeysGroup keysGroup = ManagedList.of(userKeys.getUserKeysGroups())
                            .filter(UserKeysGroup.BY_KEY_GROUP(keyGroupId))
                            .first();

                    return spawnSession(uid,
                            ownIdentity.getKeyGroup(),
                            keyGroupId,
                            ownIdentity.getIdentityKey(),
                            keysGroup.getIdentityKey(),
                            ownPreKey,
                            theirPreKey);
                }).after((r, e) -> {
                    locked.remove(uid);
                    unstashAll();
                });
    }

    /**
     * Pick session for specific keys
     *
     * @param uid        User's id
     * @param keyGroupId User's key group
     * @param ownKeyId   Own Pre Key id
     * @param theirKeyId Their Pre Key id
     */
    public Promise<PeerSession> pickSession(final int uid,
                                            final int keyGroupId,
                                            final long ownKeyId,
                                            final long theirKeyId) {

        PeerSession cached = pickCachedSession(uid, keyGroupId, ownKeyId, theirKeyId);
        if (cached != null) {
            return Promise.success(cached);
        }

        if (locked.contains(uid)) {
            stash();
            return null;
        }
        locked.add(uid);

        return Promises.tuple(
                keyManager.getOwnIdentity(),
                keyManager.getOwnPreKey(ownKeyId),
                keyManager.getUserKeyGroups(uid),
                keyManager.getUserPreKey(uid, keyGroupId, theirKeyId))
                .map(tuple -> {
                    OwnIdentity ownIdentity = tuple.getT1();
                    PrivateKey ownPreKey = tuple.getT2();
                    UserKeys userKeys = tuple.getT3();
                    PublicKey theirPreKey = tuple.getT4();

                    UserKeysGroup keysGroup = ManagedList.of(userKeys.getUserKeysGroups())
                            .filter(UserKeysGroup.BY_KEY_GROUP(keyGroupId))
                            .first();

                    return spawnSession(uid,
                            ownIdentity.getKeyGroup(),
                            keyGroupId,
                            ownIdentity.getIdentityKey(),
                            keysGroup.getIdentityKey(),
                            ownPreKey,
                            theirPreKey);
                }).after((r, e) -> {
                    locked.remove(uid);
                    unstashAll();
                });
    }

    /**
     * Spawn new session
     *
     * @param uid           user's id
     * @param ownKeyGroup   own key group id
     * @param theirKeyGroup their key group Id
     * @param ownIdentity   own identity private key
     * @param theirIdentity their identity public key
     * @param ownPreKey     own pre key
     * @param theirPreKey   their pre key
     * @return spawned session
     */
    private PeerSession spawnSession(int uid,
                                     int ownKeyGroup,
                                     int theirKeyGroup,
                                     PrivateKey ownIdentity,
                                     PublicKey theirIdentity,
                                     PrivateKey ownPreKey,
                                     PublicKey theirPreKey) {

        //
        // Calculating Master Secret
        //

        byte[] masterSecret = RatchetMasterSecret.calculateMasterSecret(
                new RatchetPrivateKey(ownIdentity.getKey()),
                new RatchetPrivateKey(ownPreKey.getKey()),
                new RatchetPublicKey(theirIdentity.getPublicKey()),
                new RatchetPublicKey(theirPreKey.getPublicKey())
        );

        //
        // Building Session
        //

        PeerSession peerSession = new PeerSession(RandomUtils.nextRid(),
                uid,
                ownKeyGroup,
                theirKeyGroup,
                ownPreKey.getKeyId(),
                theirPreKey.getKeyId(),
                masterSecret
        );

        //
        // Saving session in sessions storage
        //

        PeerSessionsStorage sessionsStorage = peerSessions.getValue(uid);
        if (sessionsStorage == null) {
            sessionsStorage = new PeerSessionsStorage(uid, new ArrayList<>());
        }
        sessionsStorage = sessionsStorage.addSession(peerSession);
        peerSessions.addOrUpdateItem(sessionsStorage);
        return peerSession;
    }

    /**
     * Picking cached session
     *
     * @param uid        User's id
     * @param keyGroupId Key Group Id
     * @return promise of session
     */
    private PeerSession pickCachedSession(int uid, final int keyGroupId) {
        return ManagedList.of(peerSessions.getValue(uid))
                .flatMap(PeerSessionsStorage.SESSIONS)
                .filter(PeerSession.BY_THEIR_GROUP(keyGroupId))
                .sorted(PeerSession.COMPARATOR)
                .firstOrNull();
    }

    /**
     * Pick cached session for specific keys
     *
     * @param uid        User's id
     * @param keyGroupId Key Group Id
     * @param ownKeyId   Own Pre key id
     * @param theirKeyId Their Pre key id
     * @return promise of session
     */
    private PeerSession pickCachedSession(int uid, final int keyGroupId, final long ownKeyId, final long theirKeyId) {
        return ManagedList.of(peerSessions.getValue(uid))
                .flatMap(PeerSessionsStorage.SESSIONS)
                .filter(PeerSession.BY_IDS(keyGroupId, ownKeyId, theirKeyId))
                .sorted(PeerSession.COMPARATOR)
                .firstOrNull();
    }

    //
    // Messages
    //

    @Override
    public Promise onAsk(Object message) throws Exception {
        if (message instanceof PickSessionForEncrypt) {
            PickSessionForEncrypt encrypt = (PickSessionForEncrypt) message;
            return pickSession(encrypt.getUid(), encrypt.getKeyGroupId());
        } else if (message instanceof PickSessionForDecrypt) {
            PickSessionForDecrypt decrypt = (PickSessionForDecrypt) message;
            return pickSession(decrypt.getUid(), decrypt.getKeyGroupId(), decrypt.getOwnPreKey(), decrypt.getTheirPreKey());
        } else {
            return super.onAsk(message);
        }
    }

    public static class PickSessionForDecrypt implements AskMessage<PeerSession> {

        private int uid;
        private int keyGroupId;
        private long theirPreKey;
        private long ownPreKey;

        public PickSessionForDecrypt(int uid, int keyGroupId, long theirPreKey, long ownPreKey) {
            this.uid = uid;
            this.keyGroupId = keyGroupId;
            this.theirPreKey = theirPreKey;
            this.ownPreKey = ownPreKey;
        }

        public int getUid() {
            return uid;
        }

        public int getKeyGroupId() {
            return keyGroupId;
        }

        public long getTheirPreKey() {
            return theirPreKey;
        }

        public long getOwnPreKey() {
            return ownPreKey;
        }
    }

    public static class PickSessionForEncrypt implements AskMessage<PeerSession> {

        private int uid;
        private int keyGroupId;

        public PickSessionForEncrypt(int uid, int keyGroupId) {
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