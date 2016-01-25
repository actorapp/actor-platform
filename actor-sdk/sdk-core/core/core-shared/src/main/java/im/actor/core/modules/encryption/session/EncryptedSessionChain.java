package im.actor.core.modules.encryption.session;

import java.util.HashSet;

import im.actor.runtime.crypto.IntegrityException;
import im.actor.runtime.crypto.box.ActorBox;
import im.actor.runtime.crypto.box.ActorBoxKey;
import im.actor.runtime.crypto.primitives.util.ByteStrings;
import im.actor.runtime.crypto.ratchet.RatchetMessageKey;
import im.actor.runtime.crypto.ratchet.RatchetPrivateKey;
import im.actor.runtime.crypto.ratchet.RatchetPublicKey;
import im.actor.runtime.crypto.ratchet.RatchetRootChainKey;

public class EncryptedSessionChain {

    private EncryptedSession session;
    private byte[] ownPrivateKey;
    private byte[] theirPublicKey;
    private HashSet<Integer> receivedCounters;
    private int sentCounter;
    private byte[] rootChainKey;

    public EncryptedSessionChain(EncryptedSession session, byte[] ownPrivateKey, byte[] theirPublicKey) {
        this.session = session;
        this.ownPrivateKey = ownPrivateKey;
        this.theirPublicKey = theirPublicKey;
        this.receivedCounters = new HashSet<Integer>();
        this.sentCounter = 0;
        this.rootChainKey = RatchetRootChainKey.makeRootChainKey(
                new RatchetPrivateKey(ownPrivateKey),
                new RatchetPublicKey(theirPublicKey),
                session.getMasterKey());
    }

    public EncryptedSession getSession() {
        return session;
    }

    public byte[] getOwnPrivateKey() {
        return ownPrivateKey;
    }

    public byte[] getTheirPublicKey() {
        return theirPublicKey;
    }

    public byte[] decrypt(byte[] data) throws IntegrityException {

        if (data.length < 88) {
            throw new IntegrityException("Data length is too small");
        }

        //
        // Parsing message header
        //

        final int senderKeyGroupId = ByteStrings.bytesToInt(data, 0);
        final long senderEphermalKey0Id = ByteStrings.bytesToLong(data, 4);
        final long receiverEphermalKey0Id = ByteStrings.bytesToLong(data, 12);
        final byte[] senderEphemeralKey = ByteStrings.substring(data, 20, 32);
        final byte[] receiverEphemeralKey = ByteStrings.substring(data, 52, 32);
        final int messageIndex = ByteStrings.bytesToInt(data, 84);

        //
        // Validating header
        //

//        if (senderKeyGroupId != session.getPeerKeyGroupId()) {
//            throw new IntegrityException("Incorrect sender key group id");
//        }
//        if (senderEphermalKey0Id != session.getTheirPreKey().getKeyId()) {
//            throw new IntegrityException("Incorrect sender pre key id");
//        }
//        if (receiverEphermalKey0Id != session.getOwnPreKey().getKeyId()) {
//            throw new IntegrityException("Incorrect receiver pre key id");
//        }
//        if (ByteStrings.isEquals(senderEphemeralKey, theirPublicKey)) {
//            throw new IntegrityException("Incorrect sender ephemeral key");
//        }
//        if (ByteStrings.isEquals(receiverEphemeralKey, ownPrivateKey)) {
//            throw new IntegrityException("Incorrect receiver ephemeral key");
//        }

        //
        // Decryption
        //

        ActorBoxKey ratchetMessageKey = RatchetMessageKey.buildKey(rootChainKey, messageIndex);
        byte[] header = ByteStrings.substring(data, 0, 88);
        byte[] message = ByteStrings.substring(data, 88, data.length - 88);
        return ActorBox.openBox(header, message, ratchetMessageKey);
    }
}
