package im.actor.core.modules.encryption.ratchet;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import im.actor.core.entity.encryption.PeerSession;
import im.actor.core.modules.ModuleContext;
import im.actor.core.modules.encryption.entity.EncryptedBox;
import im.actor.core.modules.encryption.entity.EncryptedBoxKey;
import im.actor.core.modules.encryption.entity.UserKeys;
import im.actor.core.modules.ModuleActor;
import im.actor.core.util.RandomUtils;
import im.actor.runtime.*;
import im.actor.runtime.Runtime;
import im.actor.runtime.actors.ActorRef;
import im.actor.runtime.actors.ask.AskMessage;
import im.actor.runtime.actors.ask.AskResult;
import im.actor.runtime.crypto.Cryptos;
import im.actor.runtime.crypto.IntegrityException;
import im.actor.runtime.crypto.primitives.prf.PRF;
import im.actor.runtime.function.Function;
import im.actor.runtime.promise.Promise;
import im.actor.runtime.crypto.box.ActorBox;
import im.actor.runtime.crypto.box.ActorBoxKey;
import im.actor.runtime.crypto.primitives.util.ByteStrings;
import im.actor.runtime.promise.Promises;
import im.actor.runtime.promise.PromisesArray;
import im.actor.runtime.function.Tuple2;

import static im.actor.runtime.promise.Promise.success;

public class EncryptedUserActor extends ModuleActor {

    private final String TAG;

    private final int uid;

    private int ownKeyGroupId;
    private UserKeys theirKeys;

    private HashMap<Integer, SessionHolder> activeSessions = new HashMap<>();
    private HashSet<Integer> ignoredKeyGroups = new HashSet<>();

    private boolean isReady = false;

    private final PRF keyPrf = Cryptos.PRF_SHA_STREEBOG_256();

    public EncryptedUserActor(int uid, ModuleContext context) {
        super(context);
        this.uid = uid;
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
                    ownKeyGroupId = res.getT1().getKeyGroup();
                    theirKeys = res.getT2();
                    onLoaded();
                })
                .failure(e -> {
                    Log.w(TAG, "Unable to fetch initial parameters. Freezing encryption with user #" + uid);
                    Log.e(TAG, e);
                });
    }

    private void onLoaded() {
        Log.d(TAG, "Loaded initial parameters");
        isReady = true;
        unstashAll();
    }

    private Promise<EncryptedBox> doEncrypt(final byte[] data) {


        //
        // Stage 1: Loading User Key Groups
        // Stage 2: Pick sessions for encryption
        // Stage 3: Encrypt box_key int session
        // Stage 4: Encrypt box
        //
        final byte[] encKey = Crypto.randomBytes(32);
        final byte[] encKeyExtended = keyPrf.calculate(encKey, "ActorPackage", 128);
        Log.d(TAG, "doEncrypt");
        final long start = Runtime.getActorTime();
        return PromisesArray.of(theirKeys.getUserKeysGroups())
                .filter(keysGroup -> !ignoredKeyGroups.contains(keysGroup.getKeyGroupId()))
                .mapOptional(keysGroup -> {
                    if (activeSessions.containsKey(keysGroup.getKeyGroupId())) {
                        return success(activeSessions.get(keysGroup.getKeyGroupId()).getSessions().get(0));
                    }
                    return context().getEncryption().getSessionManager()
                            .pickSession(uid, keysGroup.getKeyGroupId())
                            .failure(e -> {
                                ignoredKeyGroups.add(keysGroup.getKeyGroupId());
                            })
                            .map(src -> spawnSession(src));
                })
                .mapOptional(sessionActor -> sessionActor.getEncryptedSession().encrypt(encKeyExtended)
                        .map(r -> new Tuple2<>(r, sessionActor.getSession().getTheirKeyGroupId())))
                .zip()
                .map(src -> {

                    if (src.size() == 0) {
                        throw new RuntimeException("No sessions available");
                    }

                    Log.d(TAG, "Keys Encrypted in " + (Runtime.getActorTime() - start) + " ms");

                    ArrayList<EncryptedBoxKey> encryptedKeys = new ArrayList<>();
                    for (Tuple2<byte[], Integer> r : src) {
                        Log.d(TAG, "Keys: " + r.getT2());
                        encryptedKeys.add(new EncryptedBoxKey(uid, r.getT2(), "curve25519", r.getT1()));
                    }

                    byte[] encData;
                    try {
                        encData = ActorBox.closeBox(ByteStrings.intToBytes(ownKeyGroupId), data, Crypto.randomBytes(32), new ActorBoxKey(encKeyExtended));
                    } catch (IntegrityException e) {
                        e.printStackTrace();
                        throw new RuntimeException(e);
                    }

                    Log.d(TAG, "All Encrypted in " + (Runtime.getActorTime() - start) + " ms");

                    return new EncryptedBox(
                            encryptedKeys.toArray(new EncryptedBoxKey[encryptedKeys.size()]),
                            ByteStrings.merge(ByteStrings.intToBytes(ownKeyGroupId), encData));
                });
    }

    private Promise<byte[]> doDecrypt(final EncryptedBox data) {

        final int senderKeyGroup = ByteStrings.bytesToInt(ByteStrings.substring(data.getEncryptedPackage(), 0, 4));
        final byte[] encPackage = ByteStrings.substring(data.getEncryptedPackage(), 4, data.getEncryptedPackage().length - 4);

        //
        // Picking session
        //

        if (ignoredKeyGroups.contains(senderKeyGroup)) {
            throw new RuntimeException("This key group is ignored");
        }

        return PromisesArray.of(data.getKeys())
                .filter(EncryptedBoxKey.FILTER(myUid(), ownKeyGroupId))
                .first()
                .flatMap(boxKey -> {
                    final long senderPreKeyId = ByteStrings.bytesToLong(boxKey.getEncryptedKey(), 4);
                    final long receiverPreKeyId = ByteStrings.bytesToLong(boxKey.getEncryptedKey(), 12);

                    if (activeSessions.containsKey(boxKey.getKeyGroupId())) {
                        for (SessionActor s : activeSessions.get(senderKeyGroup).getSessions()) {
                            if (s.getSession().getOwnPreKeyId() == receiverPreKeyId &&
                                    s.getSession().getTheirPreKeyId() == senderPreKeyId) {
                                return success(new Tuple2<>(s, boxKey));
                            }
                        }
                    }
                    return context().getEncryption().getSessionManager()
                            .pickSession(uid, senderKeyGroup, receiverPreKeyId, senderPreKeyId)
                            .map(new Function<PeerSession, Tuple2<SessionActor, EncryptedBoxKey>>() {
                                @Override
                                public Tuple2<SessionActor, EncryptedBoxKey> apply(PeerSession src) {
                                    return new Tuple2<>(spawnSession(src), boxKey);
                                }
                            });
                })
                .flatMap(src -> {
                    Log.d(TAG, "Key size:" + src.getT2().getEncryptedKey().length);
                    return src.getT1().getEncryptedSession().decrypt(src.getT2().getEncryptedKey());
                })
                .map(decryptedPackage -> {
                    byte[] encData;
                    try {
                        byte[] encKeyExtended = decryptedPackage.length >= 128
                                ? decryptedPackage
                                : keyPrf.calculate(decryptedPackage, "ActorPackage", 128);
                        encData = ActorBox.openBox(ByteStrings.intToBytes(senderKeyGroup), encPackage, new ActorBoxKey(encKeyExtended));
                        Log.d(TAG, "Box size: " + encData.length);
                    } catch (IOException e) {
                        e.printStackTrace();
                        throw new RuntimeException(e);
                    }
                    return encData;
                });
    }

    private void onKeysUpdated(UserKeys userKeys) {
        this.theirKeys = userKeys;
    }

    private SessionActor spawnSession(final PeerSession session) {

        ActorRef res = system().actorOf(getPath() + "/k_" + RandomUtils.nextRid(),
                () -> new EncryptedSessionActor(context(), session));

        SessionActor cont = new SessionActor(new EncryptedSession(res), session);

        if (activeSessions.containsKey(session.getTheirKeyGroupId())) {
            activeSessions.get(session.getTheirKeyGroupId()).getSessions().add(cont);
        } else {
            ArrayList<SessionActor> l = new ArrayList<>();
            l.add(cont);
            activeSessions.put(session.getTheirKeyGroupId(), new SessionHolder(session.getTheirKeyGroupId(), l));
        }
        return cont;
    }

    //
    // Messages
    //

    @Override
    public Promise onAsk(Object message) throws Exception {
        if (!isReady) {
            stash();
            return null;
        }

        if (message instanceof EncryptBox) {
            return doEncrypt(((EncryptBox) message).getData());
        } else if (message instanceof DecryptBox) {
            return doDecrypt(((DecryptBox) message).getEncryptedBox());
        } else {
            return super.onAsk(message);
        }
    }

    @Override
    public void onReceive(Object message) {
        if (message instanceof KeyGroupUpdated) {
            if (!isReady) {
                stash();
                return;
            }
            onKeysUpdated(((KeyGroupUpdated) message).getUserKeys());
        } else {
            super.onReceive(message);
        }
    }

    public static class EncryptBox implements AskMessage<EncryptedBox> {
        private byte[] data;

        public EncryptBox(byte[] data) {
            this.data = data;
        }

        public byte[] getData() {
            return data;
        }
    }

    public static class DecryptBox implements AskMessage<byte[]> {

        private EncryptedBox encryptedBox;

        public DecryptBox(EncryptedBox encryptedBox) {
            this.encryptedBox = encryptedBox;
        }

        public EncryptedBox getEncryptedBox() {
            return encryptedBox;
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

    private class SessionHolder {

        private int keyGroupId;
        private ArrayList<SessionActor> sessions;

        public SessionHolder(int keyGroupId, ArrayList<SessionActor> sessions) {
            this.keyGroupId = keyGroupId;
            this.sessions = sessions;
        }

        public int getKeyGroupId() {
            return keyGroupId;
        }

        public ArrayList<SessionActor> getSessions() {
            return sessions;
        }
    }

    private class SessionActor {

        private EncryptedSession encryptedSession;
        private PeerSession session;

        public SessionActor(EncryptedSession encryptedSession, PeerSession session) {
            this.encryptedSession = encryptedSession;
            this.session = session;
        }

        public EncryptedSession getEncryptedSession() {
            return encryptedSession;
        }

        public PeerSession getSession() {
            return session;
        }
    }

}