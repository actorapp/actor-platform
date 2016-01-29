package im.actor.core.modules.encryption;

import java.io.IOException;
import java.util.ArrayList;

import im.actor.core.entity.encryption.PeerSession;
import im.actor.core.entity.encryption.PeerSessionsStorage;
import im.actor.core.modules.ModuleContext;
import im.actor.core.modules.encryption.entity.PrivateKey;
import im.actor.core.modules.encryption.entity.PublicKey;
import im.actor.core.modules.encryption.entity.UserKeys;
import im.actor.core.modules.encryption.entity.UserKeysGroup;
import im.actor.core.util.BaseKeyValueEngine;
import im.actor.core.util.ModuleActor;
import im.actor.core.util.RandomUtils;
import im.actor.runtime.Log;
import im.actor.runtime.Storage;
import im.actor.runtime.actors.ask.AskMessage;
import im.actor.runtime.actors.ask.AskResult;
import im.actor.runtime.collections.ManagedList;
import im.actor.runtime.crypto.ratchet.RatchetMasterSecret;
import im.actor.runtime.crypto.ratchet.RatchetPrivateKey;
import im.actor.runtime.crypto.ratchet.RatchetPublicKey;
import im.actor.runtime.function.Consumer;
import im.actor.runtime.function.Function;
import im.actor.runtime.function.FunctionTupled4;
import im.actor.runtime.function.Predicate;
import im.actor.runtime.function.Supplier;
import im.actor.runtime.promise.Promise;
import im.actor.runtime.promise.PromiseResolver;
import im.actor.runtime.promise.Promises;
import im.actor.runtime.promise.PromisesArray;
import im.actor.runtime.promise.Tuple3;
import im.actor.runtime.promise.Tuple4;
import im.actor.runtime.storage.KeyValueEngine;

public class SessionManagerActor extends ModuleActor {

    private static final String TAG = "SessionManagerActor";

    private KeyValueEngine<PeerSessionsStorage> peerSessions;
    private KeyManagerInt keyManager;

    public SessionManagerActor(ModuleContext context) {
        super(context);
    }

    @Override
    public void preStart() {
        super.preStart();
        keyManager = context().getEncryption().getKeyManagerInt();
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

    public void pickSession(final int uid,
                            final int keyGroupId,
                            final PromiseResolver<PeerSession> resolver) {

        //
        // Searching for available session
        //

        pickSession(uid, keyGroupId)
                .fallback(new Function<Exception, Promise<PeerSession>>() {
                    @Override
                    public Promise<PeerSession> apply(Exception e) {
                        return Promises.tuple(
                                keyManager.getOwnIdentity(),
                                keyManager.getOwnRandomPreKey(),
                                keyManager.getUserKeyGroups(uid),
                                keyManager.getUserRandomPreKey(uid, keyGroupId))
                                .mapPromise(new FunctionTupled4<KeyManagerActor.OwnIdentity,
                                        PrivateKey, UserKeys, PublicKey, Promise<PeerSession>>() {
                                    @Override
                                    public Promise<PeerSession> apply(KeyManagerActor.OwnIdentity ownIdentity,
                                                                      PrivateKey ownPreKey, UserKeys userKeys,
                                                                      PublicKey theirPreKey) {

                                        UserKeysGroup keysGroup = ManagedList.of(userKeys.getUserKeysGroups())
                                                .filter(UserKeysGroup.BY_KEY_GROUP(keyGroupId))
                                                .first();

                                        spawnSession(uid,
                                                ownIdentity.getKeyGroup(),
                                                keyGroupId,
                                                ownIdentity.getIdentityKey(),
                                                keysGroup.getIdentityKey(),
                                                ownPreKey,
                                                theirPreKey);

                                        return Promises.success(null);
                                    }
                                });
                    }
                })
                .afterVoid(new Supplier<Promise<PeerSession>>() {
                    @Override
                    public Promise<PeerSession> get() {
                        return pickSession(uid, keyGroupId);
                    }
                })
                .pipeTo(resolver)
                .done(self());
    }

    public void pickSession(final int uid,
                            final int keyGroupId,
                            final long ownKeyId,
                            final long theirKeyId,
                            final PromiseResolver<PeerSession> srcResolver) {

        pickSession(uid, keyGroupId, ownKeyId, theirKeyId)
                .fallback(new Function<Exception, Promise<PeerSession>>() {
                    @Override
                    public Promise<PeerSession> apply(Exception e) {
                        return Promises.tuple(
                                keyManager.getOwnIdentity(),
                                keyManager.getOwnPreKey(ownKeyId),
                                keyManager.getUserKeyGroups(uid),
                                keyManager.getUserPreKey(uid, keyGroupId, theirKeyId))
                                .map(new FunctionTupled4<KeyManagerActor.OwnIdentity, PrivateKey, UserKeys, PublicKey, PeerSession>() {
                                    @Override
                                    public PeerSession apply(KeyManagerActor.OwnIdentity ownIdentity, PrivateKey ownPreKey, UserKeys userKeys, PublicKey theirPreKey) {

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
                                    }
                                });
                    }
                })
                .pipeTo(srcResolver);
    }

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
            sessionsStorage = new PeerSessionsStorage(uid, new ArrayList<PeerSession>());
        }
        sessionsStorage = sessionsStorage.addSession(peerSession);
        peerSessions.addOrUpdateItem(sessionsStorage);
        return peerSession;
    }

    private Promise<PeerSession> pickSession(int uid, final int keyGroupId) {
        return ManagedList.of(peerSessions.getValue(uid))
                .flatMap(PeerSessionsStorage.SESSIONS)
                .filter(PeerSession.BY_THEIR_GROUP(keyGroupId))
                .firstPromise();
    }

    private Promise<PeerSession> pickSession(int uid, final int keyGroupId, final long ownKeyId, final long theirKeyId) {
        return ManagedList.of(peerSessions.getValue(uid))
                .flatMap(PeerSessionsStorage.SESSIONS)
                .filter(PeerSession.BY_IDS(keyGroupId, ownKeyId, theirKeyId))
                .firstPromise();
    }

    @Override
    public void onAsk(Object message, PromiseResolver future) {
        if (message instanceof PickSessionForEncrypt) {
            PickSessionForEncrypt encrypt = (PickSessionForEncrypt) message;
            pickSession(encrypt.getUid(), encrypt.getKeyGroupId(), future);
        } else if (message instanceof PickSessionForDecrypt) {
            PickSessionForDecrypt decrypt = (PickSessionForDecrypt) message;
            pickSession(decrypt.getUid(), decrypt.getKeyGroupId(), decrypt.getOwnPreKey(), decrypt.getTheirPreKey(),
                    future);
        } else {
            super.onAsk(message, future);
        }
    }

    public static class PickSessionForDecrypt extends AskMessage<PeerSession> {

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

    public static class PickSessionForEncrypt extends AskMessage<PeerSession> {

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