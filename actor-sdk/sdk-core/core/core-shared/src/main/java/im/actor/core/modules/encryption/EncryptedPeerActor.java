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
import im.actor.core.util.Hex;
import im.actor.core.util.ModuleActor;
import im.actor.core.util.RandomUtils;
import im.actor.runtime.Crypto;
import im.actor.runtime.Log;
import im.actor.runtime.actors.ActorCreator;
import im.actor.runtime.actors.ActorRef;
import im.actor.runtime.actors.Future;
import im.actor.runtime.actors.Props;
import im.actor.runtime.actors.ask.AskCallback;
import im.actor.runtime.crypto.IntegrityException;
import im.actor.runtime.crypto.box.ActorBox;
import im.actor.runtime.crypto.box.ActorBoxKey;
import im.actor.runtime.crypto.primitives.util.ByteStrings;

public class EncryptedPeerActor extends ModuleActor {

    private final String TAG;

    private final int uid;

    private int ownKeyGroupId;
    private UserKeys userKeys;
    private HashMap<SessionId, ActorRef> activeSessions = new HashMap<SessionId, ActorRef>();

    public EncryptedPeerActor(int uid, ModuleContext context) {
        super(context);
        this.uid = uid;
        TAG = "EncryptedPeerActor#" + uid;
    }

    @Override
    public void preStart() {
        super.preStart();

        //
        // Loading own encryption information
        //
        ask(context().getEncryption().getKeyManager(), new KeyManagerActor.FetchOwnKeyGroup(), new AskCallback() {
            @Override
            public void onResult(Object obj) {
                KeyManagerActor.FetchOwnKeyGroupResult res = (KeyManagerActor.FetchOwnKeyGroupResult) obj;
                ownKeyGroupId = res.getKeyGroupId();
            }

            @Override
            public void onError(Exception e) {
                Log.w(TAG, "Unable to fetch own key groups");
                Log.e(TAG, e);
                halt("Unable to fetch own key groups", e);
            }
        });
    }

    private void doEncrypt(final byte[] data, final Future future) {

        Log.d(TAG, "doEncrypt");
        ask(context().getEncryption().getKeyManager(), new KeyManagerActor.FetchUserKeyGroups(uid), new AskCallback() {
            @Override
            public void onResult(Object obj) {
                KeyManagerActor.FetchUserKeyGroupsResponse r = (KeyManagerActor.FetchUserKeyGroupsResponse) obj;
                UserKeysGroup[] keysGroups = r.getUserKeys().getUserKeysGroups();
                ArrayList<ActorRef> sessions = new ArrayList<ActorRef>();
                outer:
                for (final UserKeysGroup g : keysGroups) {

                    //
                    // Finding existing sessions
                    //
                    for (SessionId sessionId : activeSessions.keySet()) {
                        if (sessionId.getTheirKeyGroupId() == g.getKeyGroupId()) {
                            sessions.add(activeSessions.get(sessionId));
                            continue outer;
                        }
                    }

                    Log.d(TAG, "doEncrypt:session not found for #" + g.getKeyGroupId());
                    ask(context().getEncryption().getKeyManager(), new KeyManagerActor.FetchUserEphemeralKeyRandom(uid,
                            g.getKeyGroupId()), new AskCallback() {
                        @Override
                        public void onResult(Object obj) {
                            final KeyManagerActor.FetchUserEphemeralKeyResponse r = (KeyManagerActor.FetchUserEphemeralKeyResponse) obj;
                            Log.d(TAG, "doEncrypt:#" + g.getKeyGroupId() + " Their key ok: " + r.getEphemeralKey().getKeyId());
                            ask(context().getEncryption().getKeyManager(), new KeyManagerActor.FetchOwnEphemeralKey(), new AskCallback() {
                                @Override
                                public void onResult(Object obj) {
                                    final KeyManagerActor.FetchOwnEphemeralKeyResult res = (KeyManagerActor.FetchOwnEphemeralKeyResult) obj;
                                    Log.d(TAG, "doEncrypt:#" + g.getKeyGroupId() + " Own key ok: " + res.getId());
                                    SessionId sessionId = new SessionId(ownKeyGroupId, res.getId(),
                                            g.getKeyGroupId(), r.getEphemeralKey().getKeyId());

                                    activeSessions.put(sessionId, system().actorOf(Props.create(EncryptedSessionActor.class, new ActorCreator<EncryptedSessionActor>() {
                                        @Override
                                        public EncryptedSessionActor create() {
                                            return new EncryptedSessionActor(context(), uid,
                                                    res.getId(), r.getEphemeralKey().getKeyId(), g.getKeyGroupId());
                                        }
                                    }), getPath() + "/k_" + RandomUtils.nextRid()));

                                    doEncrypt(data, future);
                                }

                                @Override
                                public void onError(Exception e) {
                                    Log.d(TAG, "doEncrypt:#" + g.getKeyGroupId() + " Own key error");
                                    future.onError(e);
                                }
                            });
                        }

                        @Override
                        public void onError(Exception e) {
                            Log.d(TAG, "doEncrypt:#" + g.getKeyGroupId() + " Their key error");
                            future.onError(e);
                        }
                    });
                    return;
                }

                Log.d(TAG, "doEncrypt: all sessions created");
            }

            @Override
            public void onError(Exception e) {
                future.onError(e);
            }
        });


        final byte[] encKey = Crypto.randomBytes(128);

        final ArrayList<EncryptedBoxKey> encryptedKeys = new ArrayList<EncryptedBoxKey>();
        for (final SessionId sessionId : activeSessions.keySet()) {
            ask(activeSessions.get(sessionId), new EncryptedSessionActor.EncryptPackage(encKey), new AskCallback() {
                @Override
                public void onResult(Object obj) {
                    EncryptedSessionActor.EncryptedPackageRes res = (EncryptedSessionActor.EncryptedPackageRes) obj;
                    encryptedKeys.add(new EncryptedBoxKey(uid, sessionId.getTheirKeyGroupId(), res.getData()));
                    if (encryptedKeys.size() == activeSessions.size()) {
                        doEncrypt(encKey, data, encryptedKeys, future);
                    }
                }

                @Override
                public void onError(Exception e) {
                    future.onError(e);
                }
            });
        }
    }

    private void doEncrypt(byte[] encKey, byte[] data, ArrayList<EncryptedBoxKey> encryptedKeys, Future future) {
        Log.d(TAG, "doEncrypt2");
        byte[] encData;
        try {
            encData = ActorBox.closeBox(ByteStrings.intToBytes(ownKeyGroupId), data, Crypto.randomBytes(32), new ActorBoxKey(encKey));
        } catch (IntegrityException e) {
            e.printStackTrace();
            future.onError(e);
            return;
        }

        EncryptedBox encryptedBox = new EncryptedBox(
                encryptedKeys.toArray(new EncryptedBoxKey[encryptedKeys.size()]),
                ByteStrings.merge(ByteStrings.intToBytes(ownKeyGroupId), encData));

        Log.d(TAG, "doEncrypt:EncPackage: " + Hex.toHex(encData));
        for (EncryptedBoxKey k : encryptedKeys) {
            Log.d(TAG, "Key: " + Hex.toHex(k.getEncryptedKey()));
        }

        future.onResult(encryptedBox);
    }

    private void doDecrypt(final EncryptedBox data, final Future future) {

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
            future.onError(new RuntimeException("Unable to find approriate session"));
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
                    future.onError(e);
                    return;
                }

                future.onResult();
            }

            @Override
            public void onError(Exception e) {
                Log.d(TAG, "Decryption with key group:onError");
                future.onError(e);
            }
        });
    }

    @Override
    public boolean onAsk(Object message, Future future) {
        if (message instanceof EncryptPackage) {
            doEncrypt(((EncryptPackage) message).getData(), future);
            return false;
        } else if (message instanceof DecryptPackage) {
            doDecrypt(((DecryptPackage) message).getEncryptedBox(), future);
            return false;
        } else {
            return super.onAsk(message, future);
        }
    }

    @Override
    public void onReceive(Object message) {
        Log.d(TAG, "msg: " + message);
        super.onReceive(message);
    }

    public static class EncryptPackage {
        private byte[] data;

        public EncryptPackage(byte[] data) {
            this.data = data;
        }

        public byte[] getData() {
            return data;
        }
    }

    public static class DecryptPackage {

        private EncryptedBox encryptedBox;

        public DecryptPackage(EncryptedBox encryptedBox) {
            this.encryptedBox = encryptedBox;
        }

        public EncryptedBox getEncryptedBox() {
            return encryptedBox;
        }
    }
}
