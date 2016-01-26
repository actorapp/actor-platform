package im.actor.core.modules.encryption;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import im.actor.core.api.ApiMessage;
import im.actor.core.modules.ModuleContext;
import im.actor.core.modules.encryption.entity.EncryptedBox;
import im.actor.core.modules.encryption.entity.EncryptedBoxKey;
import im.actor.core.modules.encryption.entity.OwnPrivateKey;
import im.actor.core.modules.encryption.entity.SessionId;
import im.actor.core.modules.encryption.entity.UserKeys;
import im.actor.core.modules.encryption.entity.UserKeysGroup;
import im.actor.core.modules.encryption.entity.UserPublicKey;
import im.actor.core.util.Hex;
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
import im.actor.runtime.function.ArrayFunction;
import im.actor.runtime.function.Map;
import im.actor.runtime.promise.Promise;
import im.actor.runtime.promise.PromiseResolver;
import im.actor.runtime.promise.Promises;
import im.actor.runtime.crypto.IntegrityException;
import im.actor.runtime.crypto.box.ActorBox;
import im.actor.runtime.crypto.box.ActorBoxKey;
import im.actor.runtime.crypto.primitives.util.ByteStrings;
import im.actor.runtime.function.Supplier;
import im.actor.core.modules.encryption.KeyManagerActor.*;
import im.actor.core.modules.encryption.EncryptedSessionActor.*;

import static im.actor.runtime.promise.Promises.*;

public class EncryptedPeerActor extends ModuleActor {

    private final String TAG;

    private final int uid;

    private int ownKeyGroupId;
    private UserKeys theirKeys;

    private HashMap<SessionId, ActorRef> activeSessions = new HashMap<SessionId, ActorRef>();

    private boolean isReady = false;
    private ActorRef keyManager;

    public EncryptedPeerActor(int uid, ModuleContext context) {
        super(context);
        this.uid = uid;
        TAG = "EncryptedPeerActor#" + uid;
    }

    @Override
    public void preStart() {
        super.preStart();

        keyManager = context().getEncryption().getKeyManager();

        //
        // Loading own encryption information
        //
        sequence(
                ask(keyManager, new FetchOwnKeyGroup()).cast(),
                ask(keyManager, new FetchUserKeyGroups(uid)).cast()
        ).then(new Supplier<Object[]>() {
            @Override
            public void apply(Object[] objects) {
                ownKeyGroupId = ((FetchOwnKeyGroupResult) objects[0]).getKeyGroupId();
                theirKeys = ((FetchUserKeyGroupsResponse) objects[1]).getUserKeys();
                isReady = true;
                unstashAll();
            }
        }).failure(new Supplier<Exception>() {
            @Override
            public void apply(Exception e) {
                Log.w(TAG, "Unable to fetch initial parameters");
                Log.e(TAG, e);
            }
        }).dispatch(self()).done();
    }

    private void doEncrypt(final byte[] data, final PromiseResolver<EncryptBoxResponse> future) {
        Log.d(TAG, "doEncrypt");
        sequence(map(theirKeys.getUserKeysGroups(), new Map<UserKeysGroup, Promise<ActorRef>>() {
            @Override
            public Promise<ActorRef> map(final UserKeysGroup src) {
                Log.d(TAG, "doEncrypt:map");
                for (SessionId sessionId : activeSessions.keySet()) {
                    if (sessionId.getTheirKeyGroupId() == src.getKeyGroupId()) {
                        return success(activeSessions.get(sessionId));
                    }
                }

                Log.d(TAG, "doEncrypt:not_found");

                return zip(sequence(
                        ask(keyManager, new FetchUserEphemeralKeyRandom(uid, src.getKeyGroupId())).cast(),
                        ask(keyManager, new FetchOwnEphemeralKey()).cast()
                ), new ArrayFunction<Object, ActorRef>() {
                    @Override
                    public ActorRef apply(Object[] t) {

                        Log.d(TAG, "doEncrypt:not_found:apply");

                        final UserPublicKey theirEphemeral = ((FetchUserEphemeralKeyResponse) t[0]).getEphemeralKey();
                        final long ownEphemeral = ((FetchOwnEphemeralKeyResult) t[1]).getId();

                        SessionId sessionId = new SessionId(ownKeyGroupId, ownEphemeral,
                                src.getKeyGroupId(), theirEphemeral.getKeyId());
                        ActorRef res = system().actorOf(Props.create(EncryptedSessionActor.class, new ActorCreator<EncryptedSessionActor>() {
                            @Override
                            public EncryptedSessionActor create() {
                                return new EncryptedSessionActor(context(), uid,
                                        ownEphemeral, theirEphemeral.getKeyId(), src.getKeyGroupId());
                            }
                        }), getPath() + "/k_" + RandomUtils.nextRid());
                        activeSessions.put(sessionId, res);
                        return res;
                    }
                }).dispatch(self());
            }
        })).then(new Supplier<ActorRef[]>() {
            @Override
            public void apply(ActorRef[] actorRefs) {

                Log.d(TAG, "doEncrypt:enc");

                final byte[] encKey = Crypto.randomBytes(128);

                sequence(Promises.map(actorRefs, new Map<ActorRef, Promise<EncryptedPackageRes>>() {
                    @Override
                    public Promise<EncryptedPackageRes> map(ActorRef src) {
                        return ask(src, new EncryptPackage(encKey));
                    }
                })).then(new Supplier<EncryptedPackageRes[]>() {
                    @Override
                    public void apply(EncryptedPackageRes[] encryptedPackageRes) {
                        ArrayList<EncryptedBoxKey> encryptedKeys = new ArrayList<EncryptedBoxKey>();
                        for (EncryptedPackageRes r : encryptedPackageRes) {
                            encryptedKeys.add(new EncryptedBoxKey(uid, r.getKeyGroupId(), r.getData()));
                        }

                        byte[] encData;
                        try {
                            encData = ActorBox.closeBox(ByteStrings.intToBytes(ownKeyGroupId), data, Crypto.randomBytes(32), new ActorBoxKey(encKey));
                        } catch (IntegrityException e) {
                            e.printStackTrace();
                            future.error(e);
                            return;
                        }

                        EncryptedBox encryptedBox = new EncryptedBox(
                                encryptedKeys.toArray(new EncryptedBoxKey[encryptedKeys.size()]),
                                ByteStrings.merge(ByteStrings.intToBytes(ownKeyGroupId), encData));

                        future.result(new EncryptBoxResponse(encryptedBox));
                    }
                }).failure(new Supplier<Exception>() {
                    @Override
                    public void apply(Exception e) {
                        Log.d(TAG, "Unable to encrypt all receivers");
                        Log.e(TAG, e);
                        future.error(e);
                    }
                }).dispatch(self()).done();
            }
        }).dispatch(self()).done();
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