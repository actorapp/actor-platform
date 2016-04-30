package im.actor.core.modules.encryption;

import java.util.ArrayList;

import im.actor.core.entity.encryption.PeerSession;
import im.actor.core.modules.ModuleContext;
import im.actor.core.modules.encryption.session.EncryptedSessionChain;
import im.actor.core.modules.ModuleActor;
import im.actor.runtime.*;
import im.actor.runtime.actors.ask.AskMessage;
import im.actor.runtime.actors.ask.AskResult;
import im.actor.runtime.function.Consumer;
import im.actor.runtime.function.Function;
import im.actor.runtime.promise.Promise;
import im.actor.runtime.crypto.Curve25519;
import im.actor.runtime.crypto.IntegrityException;
import im.actor.runtime.crypto.primitives.util.ByteStrings;

import static im.actor.runtime.promise.Promise.success;

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

    private Promise<EncryptedPackageRes> onEncrypt(final byte[] data) {

        //
        // Stage 1: Pick Their Ephemeral key. Use already received or pick random pre key.
        // Stage 2: Pick Encryption Chain
        // Stage 3: Decrypt
        //

        return success(latestTheirEphemeralKey)
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
                });
    }

    private Promise<DecryptedPackage> onDecrypt(final byte[] data) {

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
        Log.d(TAG, "Sender Ephemeral " + Crypto.keyHash(senderEphemeralKey));
        Log.d(TAG, "Receiver Ephemeral " + Crypto.keyHash(receiverEphemeralKey));

        return pickDecryptChain(senderEphemeralKey, receiverEphemeralKey)
                .map(new Function<EncryptedSessionChain, DecryptedPackage>() {
                    @Override
                    public DecryptedPackage apply(EncryptedSessionChain encryptedSessionChain) {
                        return decrypt(encryptedSessionChain, data);
                    }
                })
                .then(new Consumer<DecryptedPackage>() {
                    @Override
                    public void apply(DecryptedPackage decryptedPackage) {
                        Log.d(TAG, "onDecrypted");
                        latestTheirEphemeralKey = senderEphemeralKey;
                    }
                })
                .failure(new Consumer<Exception>() {
                    @Override
                    public void apply(Exception e) {
                        Log.d(TAG, "onError");
                    }
                });
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

        Log.d(TAG, "!Sender Ephemeral " + Crypto.keyHash(Curve25519.keyGenPublic(chain.getOwnPrivateKey())));
        Log.d(TAG, "!Receiver Ephemeral " + Crypto.keyHash(chain.getTheirPublicKey()));

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
        return success(pickedChain)
                .flatMap(new Function<EncryptedSessionChain, Promise<EncryptedSessionChain>>() {
                    @Override
                    public Promise<EncryptedSessionChain> apply(EncryptedSessionChain src) {
                        if (src != null) {
                            return success(src);
                        }

                        // TODO: Implement!
                        return null;
//                        return ask(context().getEncryption().getKeyManager(), new FetchOwnPreKeyByPublic(ephemeralKey))
//                                .map(new Function<PrivateKey, EncryptedSessionChain>() {
//                                    @Override
//                                    public EncryptedSessionChain apply(PrivateKey src) {
//                                        EncryptedSessionChain chain = new EncryptedSessionChain(session, src.getKey(), theirEphemeralKey);
//                                        decryptionChains.add(0, chain);
//                                        if (decryptionChains.size() > MAX_DECRYPT_CHAINS) {
//                                            decryptionChains.remove(MAX_DECRYPT_CHAINS)
//                                                    .safeErase();
//                                        }
//                                        return chain;
//                                    }
//                                });
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
    public Promise onAsk(Object message) throws Exception {
        if (message instanceof EncryptPackage) {
            return onEncrypt(((EncryptPackage) message).getData());
        } else if (message instanceof DecryptPackage) {
            DecryptPackage decryptPackage = (DecryptPackage) message;
            return onDecrypt(decryptPackage.getData());
        } else {
            return super.onAsk(message);
        }
    }

    public static class EncryptPackage implements AskMessage<EncryptedPackageRes> {
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

    public static class DecryptPackage implements AskMessage<DecryptedPackage> {

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