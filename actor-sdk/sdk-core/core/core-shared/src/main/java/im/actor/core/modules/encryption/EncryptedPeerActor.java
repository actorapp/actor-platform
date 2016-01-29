package im.actor.core.modules.encryption;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import im.actor.core.api.ApiMessage;
import im.actor.core.entity.encryption.PeerSession;
import im.actor.core.modules.ModuleContext;
import im.actor.core.modules.encryption.entity.EncryptedBox;
import im.actor.core.modules.encryption.entity.EncryptedBoxKey;
import im.actor.core.modules.encryption.entity.UserKeys;
import im.actor.core.modules.encryption.entity.UserKeysGroup;
import im.actor.core.util.ModuleActor;
import im.actor.core.util.RandomUtils;
import im.actor.runtime.*;
import im.actor.runtime.Runtime;
import im.actor.runtime.actors.ActorCreator;
import im.actor.runtime.actors.ActorRef;
import im.actor.runtime.actors.Props;
import im.actor.runtime.actors.ask.AskMessage;
import im.actor.runtime.actors.ask.AskResult;
import im.actor.runtime.crypto.IntegrityException;
import im.actor.runtime.function.Consumer;
import im.actor.runtime.function.Function;
import im.actor.runtime.promise.Promise;
import im.actor.runtime.promise.PromiseResolver;
import im.actor.runtime.crypto.box.ActorBox;
import im.actor.runtime.crypto.box.ActorBoxKey;
import im.actor.runtime.crypto.primitives.util.ByteStrings;
import im.actor.runtime.promise.Promises;
import im.actor.runtime.promise.PromisesArray;
import im.actor.runtime.promise.Tuple2;

import static im.actor.runtime.promise.Promises.success;

public class EncryptedPeerActor extends ModuleActor {

    private final String TAG;

    private final int uid;

    private int ownKeyGroupId;
    private UserKeys theirKeys;

    private HashMap<Integer, SessionHolder> activeSessions = new HashMap<>();

    private boolean isReady = false;
    private KeyManagerInt keyManager;

    public EncryptedPeerActor(int uid, ModuleContext context) {
        super(context);
        this.uid = uid;
        TAG = "EncryptedPeerActor#" + uid;
    }

    @Override
    public void preStart() {
        super.preStart();

        keyManager = context().getEncryption().getKeyManagerInt();

        Promises.tuple(
                keyManager.getOwnIdentity(),
                keyManager.getUserKeyGroups(uid))
                .then(new Consumer<Tuple2<KeyManagerActor.OwnIdentity, UserKeys>>() {
                    @Override
                    public void apply(Tuple2<KeyManagerActor.OwnIdentity, UserKeys> res) {
                        Log.d(TAG, "then");
                        ownKeyGroupId = res.getT1().getKeyGroup();
                        theirKeys = res.getT2();
                        isReady = true;
                        unstashAll();
                    }
                })
                .failure(new Consumer<Exception>() {
                    @Override
                    public void apply(Exception e) {
                        Log.w(TAG, "Unable to fetch initial parameters");
                        Log.e(TAG, e);
                    }
                })
                .done(self());
    }

    private void doEncrypt(final byte[] data, final PromiseResolver<EncryptBoxResponse> future) {

        if (!isReady) {
            stash();
            return;
        }

        //
        // Stage 1: Loading User Key Groups
        // Stage 2: Pick sessions for encryption
        // Stage 3: Encrypt box_key int session
        // Stage 4: Encrypt box
        //

        final byte[] encKey = Crypto.randomBytes(128);
        Log.d(TAG, "doEncrypt");
        final long start = Runtime.getActorTime();
        PromisesArray.of(theirKeys.getUserKeysGroups())
                .mapOptional(new Function<UserKeysGroup, Promise<SessionActor>>() {
                    @Override
                    public Promise<SessionActor> apply(UserKeysGroup keysGroup) {
                        Log.d(TAG, "Key Group " + keysGroup.getKeyGroupId());
                        if (activeSessions.containsKey(keysGroup.getKeyGroupId())) {
                            return success(activeSessions.get(keysGroup.getKeyGroupId()).getSessions().get(0));
                        }
                        return context().getEncryption().getSessionManagerInt()
                                .pickSession(uid, keysGroup.getKeyGroupId())
                                .map(new Function<PeerSession, SessionActor>() {
                                    @Override
                                    public SessionActor apply(PeerSession src) {
                                        return spawnSession(src);
                                    }
                                })
                                .log(TAG + ":session#" + keysGroup.getKeyGroupId());
                    }
                })
                .mapOptional(encrypt(encKey))
                .zip()
                .map(new Function<EncryptedSessionActor.EncryptedPackageRes[], EncryptBoxResponse>() {
                    @Override
                    public EncryptBoxResponse apply(EncryptedSessionActor.EncryptedPackageRes[] src) {

                        if (src.length == 0) {
                            throw new RuntimeException("No sessions available");
                        }

                        Log.d(TAG, "Keys Encrypted in " + (Runtime.getActorTime() - start) + " ms");

                        ArrayList<EncryptedBoxKey> encryptedKeys = new ArrayList<>();
                        for (EncryptedSessionActor.EncryptedPackageRes r : src) {
                            Log.d(TAG, "Keys: " + r.getKeyGroupId());
                            encryptedKeys.add(new EncryptedBoxKey(uid, r.getKeyGroupId(), "curve25519", r.getData()));
                        }

                        byte[] encData;
                        try {
                            encData = ActorBox.closeBox(ByteStrings.intToBytes(ownKeyGroupId), data, Crypto.randomBytes(32), new ActorBoxKey(encKey));
                        } catch (IntegrityException e) {
                            e.printStackTrace();
                            throw new RuntimeException(e);
                        }

                        Log.d(TAG, "All Encrypted in " + (Runtime.getActorTime() - start) + " ms");

                        return new EncryptBoxResponse(new EncryptedBox(
                                encryptedKeys.toArray(new EncryptedBoxKey[encryptedKeys.size()]),
                                ByteStrings.merge(ByteStrings.intToBytes(ownKeyGroupId), encData)));
                    }
                })
                .pipeTo(future)
                .done(self());
    }

    private void doDecrypt(final EncryptedBox data, final PromiseResolver<DecryptBoxResponse> resolver) {

        final int senderKeyGroup = ByteStrings.bytesToInt(ByteStrings.substring(data.getEncryptedPackage(), 0, 4));
        final byte[] encPackage = ByteStrings.substring(data.getEncryptedPackage(), 4, data.getEncryptedPackage().length - 4);

        //
        // Picking session
        //

        PromisesArray.of(data.getKeys())
                .filter(EncryptedBoxKey.FILTER(myUid(), ownKeyGroupId))
                .first()
                .mapPromise(new Function<EncryptedBoxKey, Promise<Tuple2<SessionActor, EncryptedBoxKey>>>() {
                    @Override
                    public Promise<Tuple2<SessionActor, EncryptedBoxKey>> apply(final EncryptedBoxKey boxKey) {
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
                        return context().getEncryption().getSessionManagerInt()
                                .pickSession(uid, senderKeyGroup, receiverPreKeyId, senderPreKeyId)
                                .map(new Function<PeerSession, Tuple2<SessionActor, EncryptedBoxKey>>() {
                                    @Override
                                    public Tuple2<SessionActor, EncryptedBoxKey> apply(PeerSession src) {
                                        return new Tuple2<>(spawnSession(src), boxKey);
                                    }
                                });
                    }
                })
                .mapPromise(new Function<Tuple2<SessionActor, EncryptedBoxKey>, Promise<EncryptedSessionActor.DecryptedPackage>>() {
                    @Override
                    public Promise<EncryptedSessionActor.DecryptedPackage> apply(Tuple2<SessionActor, EncryptedBoxKey> src) {
                        return ask(src.getT1().getActorRef(), new EncryptedSessionActor.DecryptPackage(src.getT2().getEncryptedKey()));
                    }
                })
                .map(new Function<EncryptedSessionActor.DecryptedPackage, DecryptBoxResponse>() {
                    @Override
                    public DecryptBoxResponse apply(EncryptedSessionActor.DecryptedPackage decryptedPackage) {
                        byte[] encData;
                        try {
                            encData = ActorBox.openBox(ByteStrings.intToBytes(senderKeyGroup), encPackage, new ActorBoxKey(decryptedPackage.getData()));

                            ApiMessage message = ApiMessage.fromBytes(encData);

                            Log.d(TAG, "Box open:" + message);
                        } catch (IOException e) {
                            e.printStackTrace();
                            throw new RuntimeException(e);
                        }
                        return new DecryptBoxResponse(encData);
                    }
                })
                .pipeTo(resolver)
                .done(self());

//        PromisesArray.of(data.getKeys())
//                // Searching for compatable key
//                .filter(new Predicate<EncryptedBoxKey>() {
//                    @Override
//                    public boolean apply(EncryptedBoxKey boxKey) {
//                        return boxKey.getUid() == myUid()
//                                && boxKey.getKeyGroupId() == ownKeyGroupId
//                                && "curve25519".equals(boxKey.getKeyAlg());
//                    }
//                })
//                .first()
//                .mapPromise(new Function<EncryptedBoxKey, Promise<Tuple2<SessionActor, EncryptedBoxKey>>>() {
//                    @Override
//                    public Promise<Tuple2<SessionActor, EncryptedBoxKey>> apply(final EncryptedBoxKey boxKey) {
//                        final long senderEphermalKey0Id = ByteStrings.bytesToLong(boxKey.getEncryptedKey(), 4);
//                        final long receiverEphermalKey0Id = ByteStrings.bytesToLong(boxKey.getEncryptedKey(), 12);
//
//                        if (activeSessions.containsKey(boxKey.getKeyGroupId())) {
//                            for (SessionActor s : activeSessions.get(senderKeyGroup).getSessions()) {
//                                if (s.getOwnKeyId() == receiverEphermalKey0Id &&
//                                        s.getTheirKeyId() == senderEphermalKey0Id) {
//                                    return success(new Tuple2<>(s, boxKey));
//                                }
//                            }
//                        }
//                        return context().getEncryption().getSessionManagerInt()
//                                .pickSession(uid, senderKeyGroup, receiverEphermalKey0Id, senderEphermalKey0Id)
//                                .map(new Function<PeerSession, Tuple2<SessionActor, EncryptedBoxKey>>() {
//                                    @Override
//                                    public Tuple2<SessionActor, EncryptedBoxKey> apply(PeerSession src) {
//                                        return new Tuple2<>(spawnSession(src.getTheirKeyGroupId(), src.getTheirPreKeyId(),
//                                                src.getOwnPreKeyId()), boxKey);
//                                    }
//                                });
//                    }
//                })
//                .mapPromise(new Function<Tuple2<SessionActor, EncryptedBoxKey>, Promise<EncryptedSessionActor.DecryptedPackage>>() {
//                    @Override
//                    public Promise<EncryptedSessionActor.DecryptedPackage> apply(Tuple2<SessionActor, EncryptedBoxKey> src) {
//                        return ask(src.getT1().getActorRef(), new EncryptedSessionActor.DecryptPackage(src.getT2().getEncryptedKey()));
//                    }
//                })
//                .map(new Function<EncryptedSessionActor.DecryptedPackage, byte[]>() {
//                    @Override
//                    public byte[] apply(EncryptedSessionActor.DecryptedPackage decryptedPackage) {
//                        byte[] encData;
//                        try {
//                            encData = ActorBox.openBox(ByteStrings.intToBytes(senderKeyGroup), encPackage, new ActorBoxKey(decryptedPackage.getData()));
//
//                            ApiMessage message = ApiMessage.fromBytes(encData);
//
//                            Log.d(TAG, "Box open:" + message);
//                        } catch (IOException e) {
//                            e.printStackTrace();
//                            //future.error(e);
//                            // return;
//                        }
//                        return null;
//                    }
//                })
//                .failure(new Consumer<Exception>() {
//                    @Override
//                    public void apply(Exception e) {
//                        e.printStackTrace();
//                        resolver.error(e);
//                    }
//                })
//                .done(self());

//        Log.d(TAG, "Picking session");
//        SessionId pickedSession = null;
//        byte[] pickedMessage = null;
//        outer:
//        for (SessionId s : activeSessions.keySet()) {
//            if (s.getTheirKeyGroupId() != senderKeyGroup) {
//                continue;
//            }
//
//            for (EncryptedBoxKey k : data.getKeys()) {
//                if (k.getKeyGroupId() == ownKeyGroupId && k.getUid() == myUid()) {
//
//                    byte[] encKey = k.getEncryptedKey();
//
//                    // final int senderKeyGroupId = ByteStrings.bytesToInt(encKey, 0);
//                    final long senderEphermalKey0Id = ByteStrings.bytesToLong(encKey, 4);
//                    final long receiverEphermalKey0Id = ByteStrings.bytesToLong(encKey, 12);
//                    // final byte[] senderEphermalKey = ByteStrings.substring(encKey, 20, 32);
//                    // final byte[] receiverEphermalKey = ByteStrings.substring(encKey, 52, 32);
//                    // final int messageIndex = ByteStrings.bytesToInt(encKey, 84);
//
//                    if (s.getOwnKeyId0() == receiverEphermalKey0Id
//                            && s.getTheirKeyId0() == senderEphermalKey0Id
//                            && s.getOwnKeyGroupId() == ownKeyGroupId
//                            && s.getTheirKeyGroupId() == senderKeyGroup) {
//
//                        pickedSession = s;
//                        pickedMessage = encKey;
//                        continue outer;
//                    }
//                }
//            }
//        }
//
//        if (pickedSession == null) {
//            Log.d(TAG, "Creation session");
//            // Picking first encryption key for key group for known key group
//            byte[] encKey = null;
//            for (EncryptedBoxKey k : data.getKeys()) {
//                if (k.getKeyGroupId() == ownKeyGroupId && k.getUid() == myUid()) {
//                    encKey = k.getEncryptedKey();
//                    break;
//                }
//            }
//            if (encKey != null) {
//                final long senderEphermalKey0Id = ByteStrings.bytesToLong(encKey, 4);
//                final long receiverEphermalKey0Id = ByteStrings.bytesToLong(encKey, 12);
//
//                pickedSession = new SessionId(ownKeyGroupId, receiverEphermalKey0Id,
//                        senderKeyGroup, senderEphermalKey0Id);
//
//                Log.d(TAG, "Creation of session:" + pickedSession);
//
//                activeSessions.put(pickedSession, system().actorOf(Props.create(EncryptedSessionActor.class, new ActorCreator<EncryptedSessionActor>() {
//                    @Override
//                    public EncryptedSessionActor create() {
//                        return new EncryptedSessionActor(context(), uid, receiverEphermalKey0Id,
//                                senderEphermalKey0Id, senderKeyGroup);
//                    }
//                }), getPath() + "/k_" + senderKeyGroup + "_" + senderEphermalKey0Id + "_" + receiverEphermalKey0Id));
//                pickedMessage = encKey;
//            }
//        }
//
//        if (pickedSession == null) {
//            Log.d(TAG, "Unable to create session");
//            future.error(new RuntimeException("Unable to find approriate session"));
//            return;
//        }
//
//        ActorRef session = activeSessions.get(pickedSession);
//
//        final long start = im.actor.runtime.Runtime.getActorTime();
//        ask(session, new EncryptedSessionActor.DecryptPackage(pickedMessage), new AskCallback() {
//
//            @Override
//            public void onResult(Object obj) {
//                Log.d(TAG, "Decryption with key group:onResult " + (im.actor.runtime.Runtime.getActorTime() - start) + " ms");
//                EncryptedSessionActor.DecryptedPackage decryptedPackage = (EncryptedSessionActor.DecryptedPackage) obj;
//
//                byte[] encData;
//                try {
//                    encData = ActorBox.openBox(ByteStrings.intToBytes(senderKeyGroup), encPackage, new ActorBoxKey(decryptedPackage.getData()));
//
//                    ApiMessage message = ApiMessage.fromBytes(encData);
//
//                    Log.d(TAG, "Box open:" + message);
//                } catch (IOException e) {
//                    e.printStackTrace();
//                    future.error(e);
//                    return;
//                }
//
//                future.result(null);
//            }
//
//            @Override
//            public void onError(Exception e) {
//                Log.d(TAG, "Decryption with key group:onError");
//                future.error(e);
//            }
//        });
    }

    private SessionActor spawnSession(final PeerSession session) {

        ActorRef res = system().actorOf(Props.create(EncryptedSessionActor.class, new ActorCreator<EncryptedSessionActor>() {
            @Override
            public EncryptedSessionActor create() {
                return new EncryptedSessionActor(context(), session);
            }
        }), getPath() + "/k_" + RandomUtils.nextRid());

        SessionActor cont = new SessionActor(res, session);

        if (activeSessions.containsKey(session.getTheirKeyGroupId())) {
            activeSessions.get(session.getTheirKeyGroupId()).getSessions().add(cont);
        } else {
            ArrayList<SessionActor> l = new ArrayList<>();
            l.add(cont);
            activeSessions.put(session.getTheirKeyGroupId(), new SessionHolder(session.getTheirKeyGroupId(), l));
        }
        return cont;
    }

    private Function<SessionActor, Promise<EncryptedSessionActor.EncryptedPackageRes>> encrypt(final byte[] encKey) {
        return new Function<SessionActor, Promise<EncryptedSessionActor.EncryptedPackageRes>>() {
            @Override
            public Promise<EncryptedSessionActor.EncryptedPackageRes> apply(SessionActor sessionActor) {
                return ask(sessionActor.getActorRef(), new EncryptedSessionActor.EncryptPackage(encKey));
            }
        };
    }

    //
    // Messages
    //

    @Override
    public void onAsk(Object message, PromiseResolver future) {
        if (message instanceof EncryptBox) {
            if (!isReady) {
                stash();
                return;
            }
            doEncrypt(((EncryptBox) message).getData(), future);
        } else if (message instanceof DecryptBox) {
            if (!isReady) {
                stash();
                return;
            }
            doDecrypt(((DecryptBox) message).getEncryptedBox(), future);
        } else {
            super.onAsk(message, future);
        }
    }

    public static class EncryptBox extends AskMessage<EncryptBoxResponse> {
        private byte[] data;

        public EncryptBox(byte[] data) {
            this.data = data;
        }

        public byte[] getData() {
            return data;
        }
    }

    public static class EncryptBoxResponse extends AskResult {

        private EncryptedBox box;

        public EncryptBoxResponse(EncryptedBox box) {
            this.box = box;
        }

        public EncryptedBox getBox() {
            return box;
        }
    }

    public static class DecryptBox extends AskMessage<DecryptBoxResponse> {

        private EncryptedBox encryptedBox;

        public DecryptBox(EncryptedBox encryptedBox) {
            this.encryptedBox = encryptedBox;
        }

        public EncryptedBox getEncryptedBox() {
            return encryptedBox;
        }
    }

    public static class DecryptBoxResponse extends AskResult {

        private byte[] data;

        public DecryptBoxResponse(byte[] data) {
            this.data = data;
        }

        public byte[] getData() {
            return data;
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

        private ActorRef actorRef;
        private PeerSession session;

        public SessionActor(ActorRef actorRef, PeerSession session) {
            this.actorRef = actorRef;
            this.session = session;
        }

        public ActorRef getActorRef() {
            return actorRef;
        }

        public PeerSession getSession() {
            return session;
        }
    }
}