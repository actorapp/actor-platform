package im.actor.core.modules.encryption.ratchet;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import im.actor.core.api.ApiEncyptedBoxKey;
import im.actor.core.entity.encryption.PeerSession;
import im.actor.core.modules.ModuleContext;
import im.actor.core.modules.encryption.ratchet.entity.EncryptedUserKeys;
import im.actor.core.modules.encryption.ratchet.entity.UserKeys;
import im.actor.core.modules.ModuleActor;
import im.actor.runtime.*;
import im.actor.runtime.actors.ask.AskMessage;
import im.actor.runtime.promise.Promise;
import im.actor.runtime.crypto.primitives.util.ByteStrings;
import im.actor.runtime.promise.Promises;
import im.actor.runtime.promise.PromisesArray;

import static im.actor.runtime.promise.Promise.success;

public class EncryptedUserActor extends ModuleActor {

    private final String TAG;

    private final int uid;
    private final boolean isOwnUser;

    private int ownKeyGroupId;
    private UserKeys theirKeys;

    private HashMap<Integer, KeyGroupHolder> activeSessions = new HashMap<>();
    private HashSet<Integer> ignoredKeyGroups = new HashSet<>();

    private boolean isFreezed = true;

    public EncryptedUserActor(int uid, ModuleContext context) {
        super(context);
        this.uid = uid;
        this.isOwnUser = myUid() == uid;
        TAG = "EncryptedUserActor#" + uid;
    }

    @Override
    public void preStart() {
        super.preStart();

        KeyManager keyManager = context().getEncryption().getKeyManager();

        Promises.tuple(
                keyManager.getOwnIdentity(),
                keyManager.getUserKeyGroups(uid))
                .then(res -> {
                    Log.d(TAG, "Loaded initial parameters");
                    ownKeyGroupId = res.getT1().getKeyGroup();
                    theirKeys = res.getT2();
                    if (isOwnUser) {
                        ignoredKeyGroups.add(ownKeyGroupId);
                    }
                    isFreezed = false;
                    unstashAll();
                })
                .failure(e -> {
                    Log.w(TAG, "Unable to fetch initial parameters. Freezing encryption with user #" + uid);
                    Log.e(TAG, e);
                });
    }

    private Promise<EncryptedUserKeys> doEncrypt(byte[] data) {

        // Stage 1: Loading User Key Groups
        return wrap(PromisesArray.of(theirKeys.getUserKeysGroups())

                // Stage 1.1: Filtering invalid key groups and own key groups
                .filter(keysGroup -> !ignoredKeyGroups.contains(keysGroup.getKeyGroupId()) &&
                        (!(isOwnUser && keysGroup.getKeyGroupId() == ownKeyGroupId)))

                // Stage 2: Pick sessions for encryption
                .map(keysGroup -> {
                    if (activeSessions.containsKey(keysGroup.getKeyGroupId())) {
                        return success(activeSessions.get(keysGroup.getKeyGroupId()).first());
                    }
                    return getSessionManager()
                            .pickSession(uid, keysGroup.getKeyGroupId())
                            .map(src -> spawnSession(src))
                            .failure(e -> {
                                ignoredKeyGroups.add(keysGroup.getKeyGroupId());
                            });
                })
                .filterFailed()

                // Stage 3: Encrypt box_keys
                .map(s -> s.encrypt(data))
                .filterFailed()

                // Stage 4: Zip Everything together
                .zip(src -> new EncryptedUserKeys(uid, src, new HashSet<>(ignoredKeyGroups))));


//        final byte[] encKey = Crypto.randomBytes(32);
//        final byte[] encKeyExtended = keyPrf.calculate(encKey, "ActorPackage", 128);

        //                    byte[] encData;
//                    try {
//                        encData = ActorBox.closeBox(ByteStrings.intToBytes(ownKeyGroupId), data, Crypto.randomBytes(32), new ActorBoxKey(encKeyExtended));
//                    } catch (IntegrityException e) {
//                        e.printStackTrace();
//                        throw new RuntimeException(e);
//                    }

//                    Log.d(TAG, "All Encrypted in " + (Runtime.getActorTime() - start) + " ms");

//                    return new EncryptedUserKeys(
//                            encryptedKeys.toArray(new EncryptedBoxKey[encryptedKeys.size()]),
//                            ByteStrings.merge(ByteStrings.intToBytes(ownKeyGroupId), encData));

    }

    private Promise<byte[]> doDecrypt(int senderKeyGroupId, List<ApiEncyptedBoxKey> keys) {

//        final int senderKeyGroup = ByteStrings.bytesToInt(ByteStrings.substring(data.getEncryptedPackage(), 0, 4));
//        final byte[] encPackage = ByteStrings.substring(data.getEncryptedPackage(), 4, data.getEncryptedPackage().length - 4);

        //
        // Picking key
        //
        if (ignoredKeyGroups.contains(senderKeyGroupId)) {
            throw new RuntimeException("This key group is ignored");
        }
        ApiEncyptedBoxKey key = null;
        for (ApiEncyptedBoxKey boxKey : keys) {
            if (boxKey.getKeyGroupId() == ownKeyGroupId && boxKey.getUsersId() == myUid()) {
                key = boxKey;
                break;
            }
        }
        if (key == null) {
            throw new RuntimeException("Unable to find suitable key group's key");
        }
        final ApiEncyptedBoxKey finalKey = key;

        //
        // Decryption
        //
        long senderPreKeyId = ByteStrings.bytesToLong(key.getEncryptedKey(), 0);
        long receiverPreKeyId = ByteStrings.bytesToLong(key.getEncryptedKey(), 8);
        if (activeSessions.containsKey(key.getKeyGroupId())) {
            for (EncryptedSession s : activeSessions.get(senderKeyGroupId).getSessions()) {
                if (s.getSession().getOwnPreKeyId() == receiverPreKeyId &&
                        s.getSession().getTheirPreKeyId() == senderPreKeyId) {
                    return wrap(s.decrypt(key));
                }
            }
        }
        return wrap(getSessionManager()
                .pickSession(uid, senderKeyGroupId, receiverPreKeyId, senderPreKeyId)
                .flatMap(src -> spawnSession(src).decrypt(finalKey)));

//                .map(decryptedPackage -> {
//                    byte[] encData;
//                    try {
//                        byte[] encKeyExtended = decryptedPackage.length >= 128
//                                ? decryptedPackage
//                                : keyPrf.calculate(decryptedPackage, "ActorPackage", 128);
//                        encData = ActorBox.openBox(ByteStrings.intToBytes(senderKeyGroupId), encPackage, new ActorBoxKey(encKeyExtended));
//                        Log.d(TAG, "Box size: " + encData.length);
//                    } catch (IOException e) {
//                        e.printStackTrace();
//                        throw new RuntimeException(e);
//                    }
//                    return encData;
//                });
    }

    private void onKeysUpdated(UserKeys userKeys) {
        this.theirKeys = userKeys;
    }


    //
    // Tools
    //

    private EncryptedSession spawnSession(PeerSession peerSession) {
        EncryptedSession session = new EncryptedSession(peerSession, context());
        if (activeSessions.containsKey(peerSession.getTheirKeyGroupId())) {
            activeSessions.get(peerSession.getTheirKeyGroupId()).getSessions().add(session);
        } else {
            ArrayList<EncryptedSession> l = new ArrayList<>();
            l.add(session);
            activeSessions.put(peerSession.getTheirKeyGroupId(), new KeyGroupHolder(peerSession.getTheirKeyGroupId(), l));
        }
        return session;
    }

    private <T> Promise<T> wrap(Promise<T> p) {
        isFreezed = true;
        p.after((r, e) -> {
            isFreezed = false;
            unstashAll();
        });
        return p;
    }

    private SessionManager getSessionManager() {
        return context().getEncryption().getSessionManager();
    }


    //
    // Messages
    //

    @Override
    public Promise onAsk(Object message) throws Exception {
        if (isFreezed) {
            stash();
            return null;
        }

        if (message instanceof EncryptBox) {
            EncryptBox encryptBox = (EncryptBox) message;
            return doEncrypt(encryptBox.getData());
        } else if (message instanceof DecryptBox) {
            DecryptBox decryptBox = (DecryptBox) message;
            return doDecrypt(decryptBox.getSenderKeyGroupId(), decryptBox.getKeys());
        } else {
            return super.onAsk(message);
        }
    }

    @Override
    public void onReceive(Object message) {
        if (message instanceof KeyGroupUpdated) {
            if (isFreezed) {
                stash();
                return;
            }
            onKeysUpdated(((KeyGroupUpdated) message).getUserKeys());
        } else {
            super.onReceive(message);
        }
    }

    public static class EncryptBox implements AskMessage<EncryptedUserKeys> {
        private byte[] data;

        public EncryptBox(byte[] data) {
            this.data = data;
        }

        public byte[] getData() {
            return data;
        }
    }

    public static class DecryptBox implements AskMessage<byte[]> {

        private int senderKeyGroupId;
        private List<ApiEncyptedBoxKey> keys;

        public DecryptBox(int senderKeyGroupId, List<ApiEncyptedBoxKey> keys) {
            this.senderKeyGroupId = senderKeyGroupId;
            this.keys = keys;
        }

        public int getSenderKeyGroupId() {
            return senderKeyGroupId;
        }

        public List<ApiEncyptedBoxKey> getKeys() {
            return keys;
        }
    }

    public static class KeyGroupUpdated {

        private UserKeys userKeys;

        public KeyGroupUpdated(UserKeys userKeys) {
            this.userKeys = userKeys;
        }

        public UserKeys getUserKeys() {
            return userKeys;
        }
    }

    private class KeyGroupHolder {

        private int keyGroupId;
        private ArrayList<EncryptedSession> sessions;

        public KeyGroupHolder(int keyGroupId, ArrayList<EncryptedSession> sessions) {
            this.keyGroupId = keyGroupId;
            this.sessions = sessions;
        }

        public int getKeyGroupId() {
            return keyGroupId;
        }

        public ArrayList<EncryptedSession> getSessions() {
            return sessions;
        }

        public EncryptedSession first() {
            return sessions.get(0);
        }
    }
}