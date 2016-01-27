package im.actor.core.modules.encryption;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import im.actor.core.api.ApiMessage;
import im.actor.core.modules.ModuleContext;
import im.actor.core.modules.encryption.entity.EncryptedBox;
import im.actor.core.modules.encryption.entity.EncryptedBoxKey;
import im.actor.core.modules.encryption.entity.SessionId;
import im.actor.core.modules.encryption.entity.UserKeys;
import im.actor.core.modules.encryption.entity.UserKeysGroup;
import im.actor.core.modules.encryption.entity.UserPublicKey;
import im.actor.core.util.ModuleActor;
import im.actor.core.util.RandomUtils;
import im.actor.runtime.Crypto;
import im.actor.runtime.Log;
import im.actor.runtime.actors.ActorCreator;
import im.actor.runtime.actors.ActorRef;
import im.actor.runtime.actors.Props;
import im.actor.runtime.actors.ask.AskCallback;
import im.actor.runtime.actors.ask.AskMessage;
import im.actor.runtime.actors.ask.AskResult;
import im.actor.runtime.crypto.IntegrityException;
import im.actor.runtime.function.Consumer;
import im.actor.runtime.function.Function;
import im.actor.runtime.function.Map;
import im.actor.runtime.promise.Promise;
import im.actor.runtime.promise.PromiseResolver;
import im.actor.runtime.crypto.box.ActorBox;
import im.actor.runtime.crypto.box.ActorBoxKey;
import im.actor.runtime.crypto.primitives.util.ByteStrings;
import im.actor.runtime.promise.Promises;
import im.actor.runtime.promise.PromisesArray;

import static im.actor.runtime.promise.Promises.success;

public class EncryptedPeerActor extends ModuleActor {

    private final String TAG;

    private final int uid;

    private int ownKeyGroupId;
    private UserKeys theirKeys;

    private HashMap<SessionId, ActorRef> activeSessions = new HashMap<SessionId, ActorRef>();

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
                keyManager.getOwnGroup(),
                keyManager.getUserKeyGroups(uid))
                .then(res -> {
                    Log.d(TAG, "then");
                    ownKeyGroupId = res.getT1().getKeyGroupId();
                    theirKeys = res.getT2().getUserKeys();
                    isReady = true;
                    unstashAll();
                })
                .failure(e -> {
                    Log.w(TAG, "Unable to fetch initial parameters");
                    Log.e(TAG, e);
                })
                .done(self());
    }

    private void doEncrypt(final byte[] data, final PromiseResolver<EncryptBoxResponse> future) {
        final byte[] encKey = Crypto.randomBytes(128);
        PromisesArray.of(theirKeys.getUserKeysGroups())
                .map(new Function<UserKeysGroup, Promise<ActorRef>>() {
                    @Override
                    public Promise<ActorRef> apply(UserKeysGroup keysGroup) {
                        for (SessionId sessionId : activeSessions.keySet()) {
                            if (sessionId.getTheirKeyGroupId() == keysGroup.getKeyGroupId()) {
                                return success(activeSessions.get(sessionId));
                            }
                        }
                        return Promises.tuple(
                                keyManager.getUserRandomPreKey(uid, keysGroup.getKeyGroupId()),
                                keyManager.getOwnRandomPreKey())
                                .map(src -> spawnSession(keysGroup.getKeyGroupId(),
                                        src.getT1().getEphemeralKey(),
                                        src.getT2().getId()));
                    }
                })
                .map(new Function<ActorRef, Promise<EncryptedSessionActor.EncryptedPackageRes>>() {
                    @Override
                    public Promise<EncryptedSessionActor.EncryptedPackageRes> apply(ActorRef actorRef) {
                        return ask(actorRef, new EncryptedSessionActor.EncryptPackage(encKey));
                    }
                })
                .zip()
                .map(src -> {
                    Log.d(TAG, "res2");
                    ArrayList<EncryptedBoxKey> encryptedKeys = new ArrayList<>();
                    for (EncryptedSessionActor.EncryptedPackageRes r : src) {
                        encryptedKeys.add(new EncryptedBoxKey(uid, r.getKeyGroupId(), r.getData()));
                    }

                    byte[] encData;
                    try {
                        encData = ActorBox.closeBox(ByteStrings.intToBytes(ownKeyGroupId), data, Crypto.randomBytes(32), new ActorBoxKey(encKey));
                    } catch (IntegrityException e) {
                        e.printStackTrace();
                        throw new RuntimeException(e);
                    }

                    return new EncryptBoxResponse(new EncryptedBox(
                            encryptedKeys.toArray(new EncryptedBoxKey[encryptedKeys.size()]),
                            ByteStrings.merge(ByteStrings.intToBytes(ownKeyGroupId), encData)));
                })
                .then(encryptBoxResponse -> Log.d(TAG, "res"))
                .pipeTo(future)
                .done(self());
    }

    private void doDecrypt(final EncryptedBox data, final PromiseResolver<DecryptBoxResponse> future) {

        final int senderKeyGroup = ByteStrings.bytesToInt(ByteStrings.substring(data.getEncryptedPackage(), 0, 4));
        final byte[] encPackage = ByteStrings.substring(data.getEncryptedPackage(), 4, data.getEncryptedPackage().length - 4);

        //
        // Picking session
        //
        Log.d(TAG, "Picking session");
        SessionId pickedSession = null;
        byte[] pickedMessage = null;
        outer:
        for (SessionId s : activeSessions.keySet()) {
            if (s.getTheirKeyGroupId() != senderKeyGroup) {
                continue;
            }

            for (EncryptedBoxKey k : data.getKeys()) {
                if (k.getKeyGroupId() == ownKeyGroupId && k.getUid() == myUid()) {

                    byte[] encKey = k.getEncryptedKey();

                    // final int senderKeyGroupId = ByteStrings.bytesToInt(encKey, 0);
                    final long senderEphermalKey0Id = ByteStrings.bytesToLong(encKey, 4);
                    final long receiverEphermalKey0Id = ByteStrings.bytesToLong(encKey, 12);
                    // final byte[] senderEphermalKey = ByteStrings.substring(encKey, 20, 32);
                    // final byte[] receiverEphermalKey = ByteStrings.substring(encKey, 52, 32);
                    // final int messageIndex = ByteStrings.bytesToInt(encKey, 84);

                    if (s.getOwnKeyId0() == receiverEphermalKey0Id
                            && s.getTheirKeyId0() == senderEphermalKey0Id
                            && s.getOwnKeyGroupId() == ownKeyGroupId
                            && s.getTheirKeyGroupId() == senderKeyGroup) {

                        pickedSession = s;
                        pickedMessage = encKey;
                        continue outer;
                    }
                }
            }
        }

        if (pickedSession == null) {
            Log.d(TAG, "Creation session");
            // Picking first encryption key for key group for known key group
            byte[] encKey = null;
            for (EncryptedBoxKey k : data.getKeys()) {
                if (k.getKeyGroupId() == ownKeyGroupId && k.getUid() == myUid()) {
                    encKey = k.getEncryptedKey();
                    break;
                }
            }
            if (encKey != null) {
                final long senderEphermalKey0Id = ByteStrings.bytesToLong(encKey, 4);
                final long receiverEphermalKey0Id = ByteStrings.bytesToLong(encKey, 12);

                pickedSession = new SessionId(ownKeyGroupId, receiverEphermalKey0Id,
                        senderKeyGroup, senderEphermalKey0Id);

                Log.d(TAG, "Creation of session:" + pickedSession);

                activeSessions.put(pickedSession, system().actorOf(Props.create(EncryptedSessionActor.class, new ActorCreator<EncryptedSessionActor>() {
                    @Override
                    public EncryptedSessionActor create() {
                        return new EncryptedSessionActor(context(), uid, receiverEphermalKey0Id,
                                senderEphermalKey0Id, senderKeyGroup);
                    }
                }), getPath() + "/k_" + senderKeyGroup + "_" + senderEphermalKey0Id + "_" + receiverEphermalKey0Id));
                pickedMessage = encKey;
            }
        }

        if (pickedSession == null) {
            Log.d(TAG, "Unable to create session");
            future.error(new RuntimeException("Unable to find approriate session"));
            return;
        }

        ActorRef session = activeSessions.get(pickedSession);

        final long start = im.actor.runtime.Runtime.getActorTime();
        ask(session, new EncryptedSessionActor.DecryptPackage(pickedMessage), new AskCallback() {

            @Override
            public void onResult(Object obj) {
                Log.d(TAG, "Decryption with key group:onResult " + (im.actor.runtime.Runtime.getActorTime() - start) + " ms");
                EncryptedSessionActor.DecryptedPackage decryptedPackage = (EncryptedSessionActor.DecryptedPackage) obj;

                byte[] encData;
                try {
                    encData = ActorBox.openBox(ByteStrings.intToBytes(senderKeyGroup), encPackage, new ActorBoxKey(decryptedPackage.getData()));

                    ApiMessage message = ApiMessage.fromBytes(encData);

                    Log.d(TAG, "Box open:" + message);
                } catch (IOException e) {
                    e.printStackTrace();
                    future.error(e);
                    return;
                }

                future.result(null);
            }

            @Override
            public void onError(Exception e) {
                Log.d(TAG, "Decryption with key group:onError");
                future.error(e);
            }
        });
    }

    private ActorRef spawnSession(int theirKeyGroupId, UserPublicKey theirEphemeral, long ownKeyId) {
        SessionId sessionId = new SessionId(ownKeyGroupId, ownKeyId,
                theirKeyGroupId, theirEphemeral.getKeyId());
        ActorRef res = system().actorOf(Props.create(EncryptedSessionActor.class, () -> new EncryptedSessionActor(context(), uid,
                ownKeyId, theirEphemeral.getKeyId(), theirKeyGroupId)), getPath() + "/k_" + RandomUtils.nextRid());
        activeSessions.put(sessionId, res);
        return res;
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
}