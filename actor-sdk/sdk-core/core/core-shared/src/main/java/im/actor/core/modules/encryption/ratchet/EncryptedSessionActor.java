package im.actor.core.modules.encryption.ratchet;

import java.io.IOException;
import java.util.ArrayList;

import im.actor.core.api.ApiEncyptedBoxKey;
import im.actor.core.entity.encryption.PeerSession;
import im.actor.core.modules.ModuleContext;
import im.actor.core.modules.encryption.ratchet.entity.PrivateKey;
import im.actor.core.modules.encryption.ratchet.entity.PublicKey;
import im.actor.core.modules.ModuleActor;
import im.actor.core.modules.encryption.ratchet.entity.SessionState;
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
    // Internal State
    //

    private SessionState sessionState;
    private EncryptedSessionChain encryptionChain = null;
    private ArrayList<EncryptedSessionChain> decryptionChains = new ArrayList<>();
    private boolean isFreezed = false;

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

        sessionState = new SessionState();
        byte[] data = sessionStorage.loadItem(session.getSid());
        if (data != null) {
            try {
                sessionState = SessionState.fromBytes(data);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void saveState() {
        sessionStorage.addOrUpdateItem(session.getSid(), sessionState.toByteArray());
    }

    //
    // Encryption
    //

    private Promise<ApiEncyptedBoxKey> onEncrypt(final byte[] data) {

        // Stage 2: Pick Encryption Chain
        // Stage 3: Decrypt

        //
        // Stage 1: Pick Their Ephemeral key
        //          - After this stage we will have public their key and own private key
        //            for encryption chain
        //
        Promise<byte[]> ephemeralKey;
        if (sessionState.getLatestTheirKey() != null) {
            ephemeralKey = success(sessionState.getLatestTheirKey());
        } else {
            ephemeralKey = keyManager.getUserRandomPreKey(uid, session.getTheirKeyGroupId()).map(key -> {
                // This can be called only for the first time of sending message in this session
                // So we need to save ephemeral key and generate new initial private key
                // for this session
                sessionState = sessionState.updateKeys(key.getPublicKey());
                saveState();
                return key.getPublicKey();
            });
        }

        return wrap(ephemeralKey
                .map(publicKey -> pickEncryptChain())
                .map(encryptedSessionChain -> encrypt(encryptedSessionChain, data))
                .map(bytes -> new ApiEncyptedBoxKey(session.getUid(), session.getTheirKeyGroupId(),
                        "curve25519", bytes)));
    }

    private EncryptedSessionChain pickEncryptChain() {

        // Dispose existing encryption chain if their public keys changes
        // If not return latest one
        if (encryptionChain != null) {
            if (ByteStrings.isEquals(sessionState.getLatestTheirKey(), encryptionChain.getTheirPublicKey()) &&
                    ByteStrings.isEquals(sessionState.getLatestOwnPublicKey(), encryptionChain.getOwnPublicKey())) {
                return encryptionChain;
            } else {
                encryptionChain = null;
            }
        }

        encryptionChain = new EncryptedSessionChain(session,
                sessionState.getLatestOwnPrivateKey(),
                sessionState.getLatestOwnPublicKey(),
                sessionState.getLatestTheirKey());

        return encryptionChain;
    }

    private byte[] encrypt(EncryptedSessionChain chain, byte[] data) {
        byte[] encrypted;
        try {
            encrypted = chain.encrypt(data);
        } catch (IntegrityException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
        return encrypted;
    }

    //
    // Decryption
    //

    private Promise<byte[]> onDecrypt(ApiEncyptedBoxKey data) {

        //
        // Stage 1: Parsing message header
        // Stage 2: Picking decryption chain
        // Stage 3: Decryption of message
        // Stage 4: Saving their ephemeral key
        //

        byte[] material = data.getEncryptedKey();
        byte[] senderEphemeralKey = ByteStrings.substring(material, 16, 32);
        byte[] receiverEphemeralKey = ByteStrings.substring(material, 48, 32);

        return wrap(pickDecryptChain(senderEphemeralKey, receiverEphemeralKey)
                .map(encryptedSessionChain -> decrypt(encryptedSessionChain, material))
                .then(r -> {
                    // Update Session State keys
                    if (sessionState.getLatestTheirKey() == null ||
                            ByteStrings.isEquals(sessionState.getLatestTheirKey(), senderEphemeralKey)) {
                        sessionState = sessionState.updateKeys(senderEphemeralKey);
                        saveState();
                    }
                }));
    }


    private Promise<EncryptedSessionChain> pickDecryptChain(final byte[] theirEphemeralKey, final byte[] ephemeralKey) {
        EncryptedSessionChain pickedChain = null;
        for (EncryptedSessionChain c : decryptionChains) {
            if (ByteStrings.isEquals(c.getOwnPublicKey(), ephemeralKey)) {
                pickedChain = c;
                break;
            }
        }
        if (pickedChain != null) {
            return Promise.success(pickedChain);
        }

        return findOwnPreKey(ephemeralKey).map(privateKey -> {
            EncryptedSessionChain chain = new EncryptedSessionChain(session, privateKey,
                    ephemeralKey, theirEphemeralKey);
            decryptionChains.add(0, chain);
            if (decryptionChains.size() > MAX_DECRYPT_CHAINS) {
                decryptionChains.remove(MAX_DECRYPT_CHAINS)
                        .safeErase();
            }
            return chain;
        });
    }

    private Promise<byte[]> findOwnPreKey(byte[] ephemeralKey) {
        if (ByteStrings.isEquals(ephemeralKey, sessionState.getLatestOwnPublicKey())) {
            return Promise.success(sessionState.getLatestOwnPrivateKey());
        }
        if (ByteStrings.isEquals(ephemeralKey, sessionState.getPrevOwnPublicKey())) {
            return Promise.success(sessionState.getPrevOwnPrivateKey());
        }
        return context().getEncryption().getKeyManager().getOwnPreKey(ephemeralKey)
                .map(PrivateKey::getKey);
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
    // Tools
    //

    private <T> Promise<T> wrap(Promise<T> promise) {
        isFreezed = true;
        return promise.after((r, e) -> {
            isFreezed = false;
            unstashAll();
        });
    }

    //
    // Actor Messages
    //

    @Override
    public Promise onAsk(Object message) throws Exception {
        if (message instanceof EncryptPackage) {
            if (isFreezed) {
                stash();
                return null;
            }
            return onEncrypt(((EncryptPackage) message).getData());
        } else if (message instanceof DecryptPackage) {
            if (isFreezed) {
                stash();
                return null;
            }
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