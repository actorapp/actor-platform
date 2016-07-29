package im.actor.core.modules.encryption.ratchet;

import java.util.ArrayList;

import im.actor.core.api.ApiEncyptedBoxKey;
import im.actor.core.entity.encryption.PeerSession;
import im.actor.core.modules.ModuleContext;
import im.actor.core.modules.encryption.ratchet.entity.PublicKey;
import im.actor.core.modules.ModuleActor;
import im.actor.runtime.*;
import im.actor.runtime.actors.ask.AskMessage;
import im.actor.runtime.promise.Promise;
import im.actor.runtime.crypto.Curve25519;
import im.actor.runtime.crypto.IntegrityException;
import im.actor.runtime.crypto.primitives.util.ByteStrings;
import im.actor.runtime.storage.KeyValueStorage;

import static im.actor.runtime.promise.Promise.success;

/**
 * Double Ratchet encryption session
 * Session is identified by:
 * 1) Destination User's Id
 * 2) Own Key Group Id
 * 3) Own Pre Key Id
 * 4) Their Key Group Id
 * 5) Their Pre Key Id
 * <p>
 * During actor starting it downloads all required key from Key Manager.
 * To encrypt/decrypt messages this actor spawns encryption chains.
 */
class EncryptedSessionActor extends ModuleActor {

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
    // Convenience references
    //

    private KeyManager keyManager;
    private KeyValueStorage sessionStorage;

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
        keyManager = context().getEncryption().getKeyManager();
        sessionStorage = context().getEncryption().getKeyValueStorage();
        latestTheirEphemeralKey = sessionStorage.loadItem(session.getSid());
    }

    private Promise<ApiEncyptedBoxKey> onEncrypt(final byte[] data) {

        //
        // Stage 1: Pick Their Ephemeral key. Use already received or pick random pre key.
        // Stage 2: Pick Encryption Chain
        // Stage 3: Decrypt
        //

        Promise<byte[]> ephemeralKey;
        if (latestTheirEphemeralKey != null) {
            ephemeralKey = success(latestTheirEphemeralKey);
            Log.d(TAG, "Picked cached");
        } else {
            ephemeralKey = keyManager.getUserRandomPreKey(uid, session.getTheirKeyGroupId())
                    .map(PublicKey::getPublicKey);
            Log.d(TAG, "Picked from server #" + uid + " " + session.getTheirKeyGroupId());
        }

        return ephemeralKey
                .map(publicKey -> pickEncryptChain(publicKey))
                .map(encryptedSessionChain -> encrypt(encryptedSessionChain, data))
                .map(bytes -> new ApiEncyptedBoxKey(session.getUid(), session.getTheirKeyGroupId(),
                        "curve25519", bytes));
    }

    private Promise<byte[]> onDecrypt(ApiEncyptedBoxKey data) {

        //
        // Stage 1: Parsing message header
        // Stage 2: Picking decryption chain
        // Stage 3: Decryption of message
        // Stage 4: Saving their ephemeral key
        //

        byte[] material = data.getEncryptedKey();

        // final long ownEphemeralKey0Id = ByteStrings.bytesToLong(data, 0);
        // final long theirEphemeralKey0Id = ByteStrings.bytesToLong(data, 8);
        final byte[] senderEphemeralKey = ByteStrings.substring(material, 16, 32);
        final byte[] receiverEphemeralKey = ByteStrings.substring(material, 48, 32);
//        Log.d(TAG, "Sender Ephemeral " + Crypto.keyHash(senderEphemeralKey));
//        Log.d(TAG, "Receiver Ephemeral " + Crypto.keyHash(receiverEphemeralKey));

        return pickDecryptChain(senderEphemeralKey, receiverEphemeralKey)
                .map(encryptedSessionChain -> decrypt(encryptedSessionChain, material));
        //.then(decryptedPackage -> latestTheirEphemeralKey = senderEphemeralKey);
    }

    private EncryptedSessionChain pickEncryptChain(byte[] ephemeralKey) {

        if (latestTheirEphemeralKey == null) {
            latestTheirEphemeralKey = ephemeralKey;
            sessionStorage.addOrUpdateItem(session.getSid(), ephemeralKey);
        }

        if (encryptionChains.size() > 0) {
            return encryptionChains.get(0);
        }

        EncryptedSessionChain chain = new EncryptedSessionChain(session,
                Curve25519.keyGenPrivate(Crypto.randomBytes(32)), ephemeralKey);

        encryptionChains.add(0, chain);

        return chain;
    }

    private byte[] encrypt(EncryptedSessionChain chain, byte[] data) {

        byte[] encrypted;
        try {
            encrypted = chain.encrypt(data);
        } catch (IntegrityException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }

//        Log.d(TAG, "!Sender Ephemeral " + Crypto.keyHash(Curve25519.keyGenPublic(chain.getOwnPrivateKey())));
//        Log.d(TAG, "!Receiver Ephemeral " + Crypto.keyHash(chain.getTheirPublicKey()));

        return encrypted;
    }

    private Promise<EncryptedSessionChain> pickDecryptChain(final byte[] theirEphemeralKey, final byte[] ephemeralKey) {

        EncryptedSessionChain pickedChain = null;
        for (EncryptedSessionChain c : decryptionChains) {
            if (ByteStrings.isEquals(Curve25519.keyGenPublic(c.getOwnPrivateKey()), ephemeralKey)) {
                pickedChain = c;
                break;
            }
        }
        if (pickedChain != null) {
            return Promise.success(pickedChain);
        }


        return context().getEncryption().getKeyManager().getOwnPreKey(ephemeralKey)
                .map(src1 -> {
                    EncryptedSessionChain chain = new EncryptedSessionChain(session,
                            src1.getKey(), theirEphemeralKey);
                    decryptionChains.add(0, chain);
                    if (decryptionChains.size() > MAX_DECRYPT_CHAINS) {
                        decryptionChains.remove(MAX_DECRYPT_CHAINS)
                                .safeErase();
                    }
                    return chain;
                });
    }

    private byte[] decrypt(EncryptedSessionChain chain, byte[] data) {
        byte[] decrypted;
        try {
            decrypted = chain.decrypt(data);
        } catch (IntegrityException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
        return decrypted;
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

    public static class EncryptPackage implements AskMessage<ApiEncyptedBoxKey> {
        private byte[] data;

        public EncryptPackage(byte[] data) {
            this.data = data;
        }

        public byte[] getData() {
            return data;
        }
    }

    public static class DecryptPackage implements AskMessage<byte[]> {

        private ApiEncyptedBoxKey data;

        public DecryptPackage(ApiEncyptedBoxKey data) {
            this.data = data;
        }

        public ApiEncyptedBoxKey getData() {
            return data;
        }
    }
}