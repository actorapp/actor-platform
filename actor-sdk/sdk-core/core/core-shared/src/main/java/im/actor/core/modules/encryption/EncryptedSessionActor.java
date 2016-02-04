package im.actor.core.modules.encryption;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import im.actor.core.modules.encryption.entity.EphemeralKey;
import im.actor.core.modules.encryption.entity.PeerSession;
import im.actor.core.modules.ModuleContext;
import im.actor.core.modules.encryption.entity.PrivateKey;
import im.actor.core.modules.encryption.entity.SessionInternalState;
import im.actor.core.modules.encryption.session.EncryptedSessionChain;
import im.actor.core.util.ModuleActor;
import im.actor.runtime.*;
import im.actor.runtime.actors.ask.AskMessage;
import im.actor.runtime.actors.ask.AskResult;
import im.actor.runtime.function.Consumer;
import im.actor.runtime.function.Function;
import im.actor.runtime.promise.Promise;
import im.actor.runtime.promise.Promises;
import im.actor.runtime.crypto.Curve25519;
import im.actor.runtime.crypto.IntegrityException;
import im.actor.runtime.crypto.primitives.util.ByteStrings;

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
    private boolean isRatcheting = true;
    private EncryptedSessionChain encryptionChain = null;
    private ArrayList<EphemeralKey> lastOwnKeys = new ArrayList<>();
    private HashMap<EphemeralKey, EphemeralDecryptionChains> decryptionChains = new HashMap<>();
    private EphemeralDecryptionChains preKeyDecryptionChain = null;

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
        keyManager = encryption().getKeyManagerInt();
        byte[] data = encryption().getSessionStorage().loadItem(session.getUid());
        if (data != null) {
            try {
                SessionInternalState internalState = new SessionInternalState(data);
                latestTheirEphemeralKey = internalState.getTheirLastPublicKey();
                isRatcheting = internalState.isRatcheting();
                for (byte[] b : internalState.getOwnPrivateKeys()) {
                    lastOwnKeys.add(new EphemeralKey(b));
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private Promise<EncryptedPackageRes> onEncrypt(final byte[] data) {

        //
        // Stage 1: Pick Their Ephemeral key. Use already received or pick random pre key.
        // Stage 2: Pick Encryption Chain
        // Stage 3: Decrypt
        //

        //
        // Doing outgoing key ratcheting if needed
        // Stage 1: Generation New Key
        // Stage 2: Safely erase old key
        // Stage 3: Safely erase old encryption chain
        // Stage 4: Save session state
        //
        if (isRatcheting) {
            Log.d(TAG, "Ratcheting...");
            EphemeralKey nkey = new EphemeralKey(Curve25519.keyGenPrivate(Crypto.randomBytes(64)));
            Log.d(TAG, "New key " + Crypto.keyHash(nkey.getPublicKey()));
            lastOwnKeys.add(nkey);
            decryptionChains.put(nkey, new EphemeralDecryptionChains(nkey));
            while (lastOwnKeys.size() > 2) {
                EphemeralKey oldKey = lastOwnKeys.remove(lastOwnKeys.size() - 1);
                Log.d(TAG, "Erasing key " + Crypto.keyHash(oldKey.getPublicKey()));
                decryptionChains.remove(oldKey).safeErase();
                oldKey.safeErase();
            }
            if (encryptionChain != null) {
                encryptionChain.safeErase();
                encryptionChain = null;
            }
            isRatcheting = false;
            saveSessionState();
        }

        return Promises.success(latestTheirEphemeralKey)
                .mapIfNullPromise(keyManager.supplyUserPreKey(uid, session.getTheirKeyGroupId()))
                .map(pickEncryptChain())
                .map(encrypt(data));
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
                        if (latestTheirEphemeralKey == null || !ByteStrings.isEquals(latestTheirEphemeralKey, senderEphemeralKey)) {
                            isRatcheting = true;
                            latestTheirEphemeralKey = senderEphemeralKey;
                            saveSessionState();
                        }
                    }
                });
    }

    private Function<byte[], EncryptedSessionChain> pickEncryptChain() {
        return new Function<byte[], EncryptedSessionChain>() {
            @Override
            public EncryptedSessionChain apply(byte[] ephemeralKey) {
                if (latestTheirEphemeralKey == null) {
                    latestTheirEphemeralKey = ephemeralKey;
                    saveSessionState();
                }

                if (encryptionChain == null) {
                    encryptionChain = new EncryptedSessionChain(session, lastOwnKeys.get(0).getPrivateKey(), latestTheirEphemeralKey);
                }

                return encryptionChain;
            }
        };
    }

    private Function<EncryptedSessionChain, EncryptedPackageRes> encrypt(final byte[] data) {
        return new Function<EncryptedSessionChain, EncryptedPackageRes>() {
            @Override
            public EncryptedPackageRes apply(EncryptedSessionChain encryptedSessionChain) {
                byte[] encrypted;
                try {
                    encrypted = encryptedSessionChain.encrypt(data);
                } catch (IntegrityException e) {
                    e.printStackTrace();
                    throw new RuntimeException(e);
                }

                Log.d(TAG, "!Sender Ephemeral " + Crypto.keyHash(Curve25519.keyGenPublic(encryptedSessionChain.getOwnPrivateKey())));
                Log.d(TAG, "!Receiver Ephemeral " + Crypto.keyHash(encryptedSessionChain.getTheirPublicKey()));

                return new EncryptedPackageRes(encrypted, session.getTheirKeyGroupId());
            }
        };
    }

    private Promise<EncryptedSessionChain> pickDecryptChain(final byte[] theirEphemeralKey, final byte[] ephemeralKey) {

        //
        // Checking pre key-based chain
        //

        if (preKeyDecryptionChain != null) {
            if (ByteStrings.isEquals(preKeyDecryptionChain.getEphemeralKey().getPublicKey(), ephemeralKey)) {
                for (EncryptedSessionChain chain : preKeyDecryptionChain.getChains()) {
                    if (ByteStrings.isEquals(chain.getTheirPublicKey(), theirEphemeralKey)) {
                        return Promises.success(chain);
                    }
                }

                EncryptedSessionChain sessionChain = new EncryptedSessionChain(session,
                        preKeyDecryptionChain.getEphemeralKey().getPrivateKey(),
                        theirEphemeralKey);
                preKeyDecryptionChain.spawnNewChain(sessionChain);
                return Promises.success(sessionChain);
            }
        }
        preKeyDecryptionChain = null;

        //
        // Checking ephemeral key based chains
        //

        for (EphemeralKey e : decryptionChains.keySet()) {
            if (ByteStrings.isEquals(e.getPublicKey(), ephemeralKey)) {
                EphemeralDecryptionChains chains = decryptionChains.get(e);
                for (EncryptedSessionChain chain : chains.getChains()) {
                    if (ByteStrings.isEquals(chain.getTheirPublicKey(), theirEphemeralKey)) {
                        return Promises.success(chain);
                    }
                }
                EncryptedSessionChain sessionChain = new EncryptedSessionChain(session,
                        e.getPrivateKey(),
                        theirEphemeralKey);
                chains.spawnNewChain(sessionChain);
                return Promises.success(sessionChain);
            }
        }

        //
        // Final call to find own pre key
        //

        return context().getEncryption().getKeyManagerInt().getOwnPreKey(ephemeralKey)
                .map(new Function<PrivateKey, EncryptedSessionChain>() {
                    @Override
                    public EncryptedSessionChain apply(PrivateKey src) {
                        EncryptedSessionChain chain = new EncryptedSessionChain(session,
                                src.getKey(), theirEphemeralKey);
                        preKeyDecryptionChain = new EphemeralDecryptionChains(new EphemeralKey(src.getKey()));
                        preKeyDecryptionChain.spawnNewChain(chain);
                        // TODO: What if already created?
                        return chain;
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

    private void saveSessionState() {
        ArrayList<byte[]> ownKeys = new ArrayList<>();
        for (EphemeralKey e : lastOwnKeys) {
            ownKeys.add(e.getPrivateKey());
        }
        byte[] state = new SessionInternalState(isRatcheting, ownKeys, latestTheirEphemeralKey)
                .toByteArray();
        encryption().getSessionStorage().addOrUpdateItem(session.getUid(), state);
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

    private static class EphemeralDecryptionChains {

        private EphemeralKey ephemeralKey;
        private ArrayList<EncryptedSessionChain> chains;

        public EphemeralDecryptionChains(EphemeralKey ephemeralKey) {
            this.ephemeralKey = ephemeralKey;
            this.chains = new ArrayList<>();
        }

        public EphemeralDecryptionChains(EphemeralKey ephemeralKey, ArrayList<EncryptedSessionChain> chains) {
            this.ephemeralKey = ephemeralKey;
            this.chains = chains;
        }

        public EphemeralKey getEphemeralKey() {
            return ephemeralKey;
        }

        public ArrayList<EncryptedSessionChain> getChains() {
            return chains;
        }

        public void safeErase() {

        }

        public void spawnNewChain(EncryptedSessionChain chain) {
            chains.add(0, chain);
            while (chains.size() > 2) {
                chains.remove(chains.size() - 1).safeErase();
            }
        }
    }
}