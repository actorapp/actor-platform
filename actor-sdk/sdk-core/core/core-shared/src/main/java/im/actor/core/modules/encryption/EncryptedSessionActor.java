package im.actor.core.modules.encryption;

import java.util.ArrayList;

import im.actor.core.modules.ModuleContext;
import im.actor.core.modules.encryption.entity.OwnPrivateKey;
import im.actor.core.modules.encryption.entity.UserKeysGroup;
import im.actor.core.modules.encryption.entity.UserPublicKey;
import im.actor.core.modules.encryption.session.EncryptedSession;
import im.actor.core.modules.encryption.session.EncryptedSessionChain;
import im.actor.core.util.ModuleActor;
import im.actor.runtime.*;
import im.actor.runtime.actors.ActorRef;
import im.actor.runtime.actors.ask.AskCallback;
import im.actor.runtime.actors.ask.AskMessage;
import im.actor.runtime.actors.ask.AskResult;
import im.actor.runtime.promise.PromiseResolver;
import im.actor.runtime.promise.Promises;
import im.actor.runtime.crypto.Curve25519;
import im.actor.runtime.crypto.IntegrityException;
import im.actor.runtime.crypto.primitives.util.ByteStrings;
import im.actor.core.modules.encryption.KeyManagerActor.*;
import im.actor.runtime.function.ArrayFunction;
import im.actor.runtime.function.Consumer;

public class EncryptedSessionActor extends ModuleActor {

    private final String TAG;

    //
    // Key References
    //

    private final long ownKey0;
    private final int uid;
    private final long theirKey0;
    private final int theirKeyGroup;

    //
    // Loaded Session
    //

    private EncryptedSession session;

    //
    // Temp encryption chains
    //

    private byte[] theirEphemeralKey;
    private ArrayList<EncryptedSessionChain> chains = new ArrayList<EncryptedSessionChain>();

    public EncryptedSessionActor(ModuleContext context, int uid, long ownKey0, long theirKey0,
                                 int theirKeyGroup) {
        super(context);
        this.TAG = "EncryptionSessionActor#" + uid + "_" + theirKeyGroup;
        this.uid = uid;
        this.ownKey0 = ownKey0;
        this.theirKey0 = theirKey0;
        this.theirKeyGroup = theirKeyGroup;
    }

    @Override
    public void preStart() {
        Log.d(TAG, "preStart");
        ActorRef keyManager = context().getEncryption().getKeyManager();
        Promises.zip(Promises.sequence(
                ask(keyManager, new FetchOwnKey()).cast(),
                ask(keyManager, new FetchEphemeralPrivateKeyById(ownKey0)).cast(),
                ask(keyManager, new FetchUserEphemeralKey(uid, theirKeyGroup, theirKey0)).cast(),
                ask(keyManager, new FetchUserKeyGroups(uid)).cast()
        ), new ArrayFunction<Object, EncryptedSession>() {
            @Override
            public EncryptedSession apply(Object[] objects) {
                OwnPrivateKey ownIdentityKey = ((FetchOwnKeyResult) objects[0]).getIdentityKey();
                OwnPrivateKey ownPreKey = new OwnPrivateKey(ownKey0, "curve25519",
                        ((FetchEphemeralPrivateKeyRes) objects[1]).getPrivateKey());
                UserPublicKey theirPreKey = ((FetchUserEphemeralKeyResponse) objects[2]).getEphemeralKey();

                FetchUserKeyGroupsResponse keyGroups = (FetchUserKeyGroupsResponse) objects[3];
                UserKeysGroup keysGroup = null;
                for (UserKeysGroup g : keyGroups.getUserKeys().getUserKeysGroups()) {
                    if (g.getKeyGroupId() == theirKeyGroup) {
                        keysGroup = g;
                        break;
                    }
                }
                if (keysGroup == null) {
                    Log.w(TAG, "Their key group not found");
                    throw new RuntimeException("Their key group not found");
                }
                UserPublicKey theirIdentityKey = keysGroup.getIdentityKey();

                return new EncryptedSession(ownIdentityKey, ownPreKey, theirIdentityKey, theirPreKey, theirKeyGroup);
            }
        }).then(new Consumer<EncryptedSession>() {
            @Override
            public void apply(EncryptedSession encryptedSession) {
                EncryptedSessionActor.this.session = encryptedSession;
                unstashAll();
            }
        }).failure(new Consumer<Exception>() {
            @Override
            public void apply(Exception e) {
                Log.w(TAG, "Session load error");
                Log.e(TAG, e);
            }
        }).done(self());
    }

    private void onEncrypt(final byte[] data, final PromiseResolver<EncryptedPackageRes> future) {
        if (session == null) {
            stash();
            return;
        }

        if (theirEphemeralKey == null) {
            ask(context().getEncryption().getKeyManager(), new KeyManagerActor.FetchUserEphemeralKeyRandom(uid, theirKeyGroup), new AskCallback() {
                @Override
                public void onResult(Object obj) {
                    KeyManagerActor.FetchUserEphemeralKeyResponse response = (KeyManagerActor.FetchUserEphemeralKeyResponse) obj;
                    if (theirEphemeralKey == null) {
                        theirEphemeralKey = response.getEphemeralKey().getPublicKey();
                    }
                    onEncrypt(data, future);
                }

                @Override
                public void onError(Exception e) {
                    future.error(e);
                }
            });
            return;
        }

        if (chains.size() == 0) {
            spawnChain(Curve25519.keyGenPrivate(Crypto.randomBytes(32)), theirEphemeralKey);
        }

        try {
            future.result(new EncryptedPackageRes(chains.get(0).encrypt(data), theirKeyGroup));
        } catch (IntegrityException e) {
            e.printStackTrace();
            future.error(e);
        }
    }

    private void onDecrypt(final byte[] data, final PromiseResolver<DecryptedPackage> future) {
        if (session == null) {
            stash();
            return;
        }

        // final int ownKeyGroupId = ByteStrings.bytesToInt(data, 0);
        // final long ownEphermalKey0Id = ByteStrings.bytesToLong(data, 4);
        // final long theirEphermalKey0Id = ByteStrings.bytesToLong(data, 12);
        final byte[] senderEphemeralKey = ByteStrings.substring(data, 20, 32);
        final byte[] receiverEphemeralKey = ByteStrings.substring(data, 52, 32);
        final int messageIndex = ByteStrings.bytesToInt(data, 84);

        EncryptedSessionChain pickedChain = null;
        for (EncryptedSessionChain c : chains) {
            if (ByteStrings.isEquals(Curve25519.keyGenPublic(c.getOwnPrivateKey()), receiverEphemeralKey)) {
                pickedChain = c;
                break;
            }
        }

        if (pickedChain == null) {
            ask(context().getEncryption().getKeyManager(), new KeyManagerActor.FetchEphemeralPrivateKey(receiverEphemeralKey), new AskCallback() {

                @Override
                public void onResult(Object obj) {
                    final KeyManagerActor.FetchEphemeralPrivateKeyRes theirEphermalKey = (KeyManagerActor.FetchEphemeralPrivateKeyRes) obj;
                    spawnChain(theirEphermalKey.getPrivateKey(), senderEphemeralKey);
                    onDecrypt(data, future);
                }

                @Override
                public void onError(Exception e) {
                    future.error(e);
                }
            });
            return;
        }

        try {
            byte[] decrypted = pickedChain.decrypt(data);
            theirEphemeralKey = senderEphemeralKey;
            future.result(new DecryptedPackage(decrypted));
        } catch (IntegrityException e) {
            e.printStackTrace();
            future.error(e);
        }
    }

    private EncryptedSessionChain spawnChain(final byte[] privateKey, final byte[] publicKey) {
        EncryptedSessionChain chain = new EncryptedSessionChain(session, privateKey, publicKey);
        chains.add(0, chain);
        return chain;
    }

    @Override
    public void onAsk(Object message, PromiseResolver future) {
        Log.d(TAG, "onAsk");
        if (message instanceof EncryptPackage) {
            onEncrypt(((EncryptPackage) message).getData(), future);
        } else if (message instanceof DecryptPackage) {
            DecryptPackage decryptPackage = (DecryptPackage) message;
            onDecrypt(decryptPackage.getData(), future);
        } else {
            super.onAsk(message, future);
        }
    }

    public static class EncryptPackage extends AskMessage<EncryptedPackageRes> {
        private byte[] data;

        public EncryptPackage(byte[] data) {
            this.data = data;
        }

        public byte[] getData() {
            return data;
        }
    }

    public static class EncryptedPackageRes extends AskResult {

        private byte[] data;
        private int keyGroupId;

        public EncryptedPackageRes(byte[] data, int keyGroupId) {
            this.data = data;
            this.keyGroupId = keyGroupId;
        }

        public byte[] getData() {
            return data;
        }

        public int getKeyGroupId() {
            return keyGroupId;
        }
    }

    public static class DecryptPackage {

        private byte[] data;

        public DecryptPackage(byte[] data) {
            this.data = data;
        }

        public byte[] getData() {
            return data;
        }
    }

    public static class DecryptedPackage {

        private byte[] data;

        public DecryptedPackage(byte[] data) {
            this.data = data;
        }

        public byte[] getData() {
            return data;
        }
    }
}