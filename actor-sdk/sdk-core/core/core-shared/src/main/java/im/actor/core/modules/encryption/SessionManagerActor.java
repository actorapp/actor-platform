package im.actor.core.modules.encryption;

import java.io.IOException;
import java.util.ArrayList;

import im.actor.core.entity.encryption.PeerSession;
import im.actor.core.entity.encryption.PeerSessionsStorage;
import im.actor.core.modules.ModuleContext;
import im.actor.core.modules.encryption.entity.PublicKey;
import im.actor.core.util.BaseKeyValueEngine;
import im.actor.core.util.ModuleActor;
import im.actor.core.util.RandomUtils;
import im.actor.runtime.Log;
import im.actor.runtime.Storage;
import im.actor.runtime.actors.ask.AskMessage;
import im.actor.runtime.actors.ask.AskResult;
import im.actor.runtime.function.Consumer;
import im.actor.runtime.function.Function;
import im.actor.runtime.function.Predicate;
import im.actor.runtime.promise.Promise;
import im.actor.runtime.promise.PromiseResolver;
import im.actor.runtime.promise.Promises;
import im.actor.runtime.promise.PromisesArray;
import im.actor.runtime.promise.Tuple3;
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

    public void pickSession(final int uid, final int keyGroupId, final PromiseResolver<PickSessionResp> resolver) {

        //
        // Searching for available session
        //

        pickSession(uid, keyGroupId)
                .then(new Consumer<PeerSession>() {
                    @Override
                    public void apply(PeerSession session) {
                        resolver.result(new PickSessionResp(session));
                    }
                })
                .failure(new Consumer<Exception>() {
                    @Override
                    public void apply(Exception e) {
                        Promises.tuple(
                                keyManager.getUserRandomPreKey(uid, keyGroupId),
                                keyManager.getOwnRandomPreKey(),
                                keyManager.getOwnGroup())
                                .then(new Consumer<Tuple3<PublicKey, KeyManagerActor.FetchOwnEphemeralKeyResult, KeyManagerActor.FetchOwnKeyGroupResult>>() {
                                    @Override
                                    public void apply(Tuple3<PublicKey, KeyManagerActor.FetchOwnEphemeralKeyResult, KeyManagerActor.FetchOwnKeyGroupResult> res) {
                                        spawnSession(uid,
                                                res.getT3().getKeyGroupId(),
                                                keyGroupId,
                                                res.getT2().getId(),
                                                res.getT1().getKeyId());

                                        pickSession(uid, keyGroupId)
                                                .map(new Function<PeerSession, PickSessionResp>() {
                                                    @Override
                                                    public PickSessionResp apply(PeerSession session) {
                                                        return new PickSessionResp(session);
                                                    }
                                                })
                                                .pipeTo(resolver).done(self())
                                                .log(TAG + ":pick(internal)");
                                    }
                                })
                                .failure(new Consumer<Exception>() {
                                    @Override
                                    public void apply(Exception e) {
                                        resolver.error(e);
                                    }
                                })
                                .log(TAG + ":pick(key_manager)")
                                .done(self());
                    }
                })
                .log(TAG + ":pick(outer)")
                .done(self());
    }

    public void pickSession(final int uid, final int keyGroupId, final long ownKeyId, final long theirKeyId, final PromiseResolver<PickSessionResp> srcResolver) {

        pickSession(uid, keyGroupId, ownKeyId, theirKeyId)
                .then(new Consumer<PeerSession>() {
                    @Override
                    public void apply(PeerSession session) {
                        srcResolver.result(new PickSessionResp(session));
                    }
                })
                .failure(new Consumer<Exception>() {
                    @Override
                    public void apply(Exception e) {
                        keyManager.getOwnGroup()
                                .then(new Consumer<KeyManagerActor.FetchOwnKeyGroupResult>() {
                                    @Override
                                    public void apply(KeyManagerActor.FetchOwnKeyGroupResult fetchOwnKeyGroupResult) {
                                        spawnSession(uid,
                                                fetchOwnKeyGroupResult.getKeyGroupId(),
                                                keyGroupId,
                                                ownKeyId,
                                                theirKeyId);
                                        pickSession(uid, keyGroupId, keyGroupId, theirKeyId)
                                                .map(new Function<PeerSession, PickSessionResp>() {
                                                    @Override
                                                    public PickSessionResp apply(PeerSession session) {
                                                        return new PickSessionResp(session);
                                                    }
                                                })
                                                .pipeTo(srcResolver)
                                                .done(self());
                                    }
                                })
                                .failure(new Consumer<Exception>() {
                                    @Override
                                    public void apply(Exception e) {
                                        srcResolver.tryError(e);
                                    }
                                })
                                .done(self());
                    }
                })
                .done(self());

    }

    private void spawnSession(int uid, int ownKeyGroup, int theirKeyGroup, long ownPreKeyId,
                              long theirPreKeyId) {
        PeerSessionsStorage sessionsStorage = peerSessions.getValue(uid);
        if (sessionsStorage == null) {
            sessionsStorage = new PeerSessionsStorage(uid, new ArrayList<PeerSession>());
        }
        sessionsStorage = sessionsStorage.addSession(new PeerSession(
                RandomUtils.nextRid(),
                uid,
                ownKeyGroup,
                theirKeyGroup,
                ownPreKeyId,
                theirPreKeyId
        ));
        peerSessions.addOrUpdateItem(sessionsStorage);
    }

    private Promise<PeerSession> pickSession(int uid, final int keyGroupId) {
        return PromisesArray.of(peerSessions.getValue(uid))
                .flatMap(new Function<PeerSessionsStorage, PeerSession[]>() {
                    @Override
                    public PeerSession[] apply(PeerSessionsStorage peerSessionsStorage) {
                        if (peerSessionsStorage == null) {
                            return new PeerSession[0];
                        }
                        return peerSessionsStorage.getSessions();
                    }
                })
                .filter(new Predicate<PeerSession>() {
                    @Override
                    public boolean apply(PeerSession session) {
                        return session.getTheirKeyGroupId() == keyGroupId;
                    }
                })
                .first().cast();
    }

    private Promise<PeerSession> pickSession(int uid, final int keyGroupId, final long ownKeyId, final long theirKeyId) {
        return PromisesArray.of(peerSessions.getValue(uid))
                .flatMap(new Function<PeerSessionsStorage, PeerSession[]>() {
                    @Override
                    public PeerSession[] apply(PeerSessionsStorage peerSessionsStorage) {
                        if (peerSessionsStorage == null) {
                            return new PeerSession[0];
                        }
                        return peerSessionsStorage.getSessions();
                    }
                })
                .filter(new Predicate<PeerSession>() {
                    @Override
                    public boolean apply(PeerSession session) {
                        return session.getTheirKeyGroupId() == keyGroupId && session.getOwnPreKeyId() == ownKeyId
                                && session.getTheirPreKeyId() == theirKeyId;
                    }
                })
                .first().cast();
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

    public static class PickSessionForDecrypt extends AskMessage<PickSessionResp> {

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

    public static class PickSessionForEncrypt extends AskMessage<PickSessionResp> {

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

    public static class PickSessionResp extends AskResult {
        private PeerSession session;

        public PickSessionResp(PeerSession session) {
            this.session = session;
        }

        public PeerSession getSession() {
            return session;
        }
    }
}