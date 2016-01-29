package im.actor.core.modules.encryption;

import java.util.ArrayList;

import im.actor.core.entity.encryption.PeerSession;
import im.actor.core.modules.ModuleContext;
import im.actor.core.modules.encryption.entity.PrivateKey;
import im.actor.core.modules.encryption.entity.UserKeys;
import im.actor.core.modules.encryption.entity.UserKeysGroup;
import im.actor.core.modules.encryption.entity.PublicKey;
import im.actor.core.modules.encryption.session.EncryptedSession;
import im.actor.core.modules.encryption.session.EncryptedSessionChain;
import im.actor.core.util.ModuleActor;
import im.actor.runtime.*;
import im.actor.runtime.actors.ActorRef;
import im.actor.runtime.actors.ask.AskMessage;
import im.actor.runtime.actors.ask.AskResult;
import im.actor.runtime.function.Consumer;
import im.actor.runtime.function.Function;
import im.actor.runtime.promise.Promise;
import im.actor.runtime.promise.PromiseResolver;
import im.actor.runtime.promise.Promises;
import im.actor.runtime.crypto.Curve25519;
import im.actor.runtime.crypto.IntegrityException;
import im.actor.runtime.crypto.primitives.util.ByteStrings;
import im.actor.core.modules.encryption.KeyManagerActor.*;
import im.actor.runtime.promise.Tuple4;

/**
 * Axolotl Ratchet encryption session
 * Session is identified by:
 * 1) Destination User's Id
 * 2) Own Key Group Id
 * 3) Own Pre Key Id
 * 4) Their Key Group Id
 * 5) Their Pre Key Id
 * <p/>
 * During actor starting it downloads all required key from Key Manager.
 * To encrypt/decrypt messages this actor spawns encryption chains.
 */
public class EncryptedSessionActor extends ModuleActor {

    private final String TAG;

    // No need to keep too much decryption chains as all messages are sequenced. Newer messages
    // never intentionally use old keys, but there are cases when some messages can be sent with
    // old encryption keys right after messages with new one. Even when we will kill sequence
    // new actors can be easily started again with same keys.
    // TODO: Check if this can cause race condition
    private final int MAX_DECRYPT_CHAINS = 2;

    //
    // Key References
    //

    private final int uid;
    private final PeerSession session;

    //
    // Key Manager reference
    //

    private KeyManagerInt keyManager;

    //
    // Temp encryption chains
    //

    private byte[] latestTheirEphemeralKey;
    private ArrayList<EncryptedSessionChain> encryptionChains = new ArrayList<>();
    private ArrayList<EncryptedSessionChain> decryptionChains = new ArrayList<>();

    //
    // Constructors and Methods
    //

    public EncryptedSessionActor(ModuleContext context, PeerSession session) {
        super(context);
        this.TAG = "EncryptionSessionActor#" + session.getUid() + "_" + session.getTheirKeyGroupId();
        this.uid = session.getUid();
        this.session = session;
    }

    @Override
    public void preStart() {
        super.preStart();
        keyManager = context().getEncryption().getKeyManagerInt();
    }

    private void onEncrypt(final byte[] data, final PromiseResolver<EncryptedPackageRes> future) {

        //
        // Stage 1: Pick Their Ephemeral key. Use already received or pick random pre key.
        // Stage 2: Pick Encryption Chain
        // Stage 3: Decrypt
        //

        Promises.success(latestTheirEphemeralKey)
                .mapIfNullPromise(keyManager.supplyUserPreKey(uid, session.getTheirKeyGroupId()))
                .map(new Function<byte[], EncryptedSessionChain>() {
                    @Override
                    public EncryptedSessionChain apply(byte[] publicKey) {
                        return pickEncryptChain(publicKey);
                    }
                })
                .map(new Function<EncryptedSessionChain, EncryptedPackageRes>() {
                    @Override
                    public EncryptedPackageRes apply(EncryptedSessionChain encryptedSessionChain) {
                        return encrypt(encryptedSessionChain, data);
                    }
                })
                .pipeTo(future)
                .done(self());
    }

    private void onDecrypt(final byte[] data, final PromiseResolver<DecryptedPackage> future) {

        //
        // Stage 1: Parsing message header
        // Stage 2: Picking decryption chain
        // Stage 3: Decryption of message
        // Stage 4: Saving their ephemeral key
        //

        // final int ownKeyGroupId = ByteStrings.bytesToInt(data, 0);
        // final long ownEphemeralKey0Id = ByteStrings.bytesToLong(data, 4);
        // final long theirEphemeralKey0Id = ByteStrings.bytesToLong(data, 12);
        final byte[] senderEphemeralKey = ByteStrings.substring(data, 20, 32);
        final byte[] receiverEphemeralKey = ByteStrings.substring(data, 52, 32);
        Log.d(TAG, "Sender Ephemeral " + Crypto.hex(senderEphemeralKey));
        Log.d(TAG, "Receiver Ephemeral " + Crypto.hex(receiverEphemeralKey));

        pickDecryptChain(senderEphemeralKey, receiverEphemeralKey)
                .map(new Function<EncryptedSessionChain, DecryptedPackage>() {
                    @Override
                    public DecryptedPackage apply(EncryptedSessionChain encryptedSessionChain) {
                        return decrypt(encryptedSessionChain, data);
                    }
                })
                .pipeTo(future)
                .then(new Consumer<DecryptedPackage>() {
                    @Override
                    public void apply(DecryptedPackage decryptedPackage) {
                        latestTheirEphemeralKey = senderEphemeralKey;
                    }
                })
                .done(self());
    }

    private EncryptedSessionChain pickEncryptChain(byte[] ephemeralKey) {

        if (latestTheirEphemeralKey == null) {
            latestTheirEphemeralKey = ephemeralKey;
        }

        if (encryptionChains.size() > 0) {
            return encryptionChains.get(0);
        }

        EncryptedSessionChain chain = new EncryptedSessionChain(session, Curve25519.keyGenPrivate(Crypto.randomBytes(32)), ephemeralKey);
        encryptionChains.add(0, chain);
        return chain;
    }

    private EncryptedPackageRes encrypt(EncryptedSessionChain chain, byte[] data) {

        byte[] encrypted;
        try {
            encrypted = chain.encrypt(data);
        } catch (IntegrityException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
        return new EncryptedPackageRes(encrypted, session.getTheirKeyGroupId());
    }

    private Promise<EncryptedSessionChain> pickDecryptChain(final byte[] theirEphemeralKey, final byte[] ephemeralKey) {
        EncryptedSessionChain pickedChain = null;
        for (EncryptedSessionChain c : decryptionChains) {
            if (ByteStrings.isEquals(Curve25519.keyGenPublic(c.getOwnPrivateKey()), ephemeralKey)) {
                pickedChain = c;
                break;
            }
        }
        return Promises.success(pickedChain)
                .mapPromise(new Function<EncryptedSessionChain, Promise<EncryptedSessionChain>>() {
                    @Override
                    public Promise<EncryptedSessionChain> apply(EncryptedSessionChain src) {
                        if (src != null) {
                            return Promises.success(src);
                        }

                        return ask(context().getEncryption().getKeyManager(), new FetchOwnPreKeyByPublic(ephemeralKey))
                                .map(new Function<PrivateKey, EncryptedSessionChain>() {
                                    @Override
                                    public EncryptedSessionChain apply(PrivateKey src) {
                                        EncryptedSessionChain chain = new EncryptedSessionChain(session, src.getKey(), theirEphemeralKey);
                                        decryptionChains.add(0, chain);
                                        if (decryptionChains.size() > MAX_DECRYPT_CHAINS) {
                                            decryptionChains.remove(MAX_DECRYPT_CHAINS)
                                                    .safeErase();
                                        }
                                        return chain;
                                    }
                                });
                    }
                });
    }

    private DecryptedPackage decrypt(EncryptedSessionChain chain, byte[] data) {
        byte[] decrypted;
        try {
            decrypted = chain.decrypt(data);
        } catch (IntegrityException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
        return new DecryptedPackage(decrypted);
    }

    //
    // Actor Messages
    //

    @Override
    public void onAsk(Object message, PromiseResolver resolver) {
        if (message instanceof EncryptPackage) {
            onEncrypt(((EncryptPackage) message).getData(), resolver);
        } else if (message instanceof DecryptPackage) {
            DecryptPackage decryptPackage = (DecryptPackage) message;
            onDecrypt(decryptPackage.getData(), resolver);
        } else {
            super.onAsk(message, resolver);
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

    public static class DecryptPackage extends AskMessage<DecryptedPackage> {

        private byte[] data;

        public DecryptPackage(byte[] data) {
            this.data = data;
        }

        public byte[] getData() {
            return data;
        }
    }

    public static class DecryptedPackage extends AskResult {

        private byte[] data;

        public DecryptedPackage(byte[] data) {
            this.data = data;
        }

        public byte[] getData() {
            return data;
        }
    }
}