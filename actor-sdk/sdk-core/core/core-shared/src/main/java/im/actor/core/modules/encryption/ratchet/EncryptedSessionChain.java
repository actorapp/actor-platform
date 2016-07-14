package im.actor.core.modules.encryption.ratchet;

import im.actor.core.entity.encryption.PeerSession;
import im.actor.core.util.RandomUtils;
import im.actor.runtime.Crypto;
import im.actor.runtime.crypto.Curve25519;
import im.actor.runtime.crypto.IntegrityException;
import im.actor.runtime.crypto.box.ActorBox;
import im.actor.runtime.crypto.box.ActorBoxKey;
import im.actor.runtime.crypto.primitives.util.ByteStrings;
import im.actor.runtime.crypto.ratchet.RatchetMessageKey;
import im.actor.runtime.crypto.ratchet.RatchetPrivateKey;
import im.actor.runtime.crypto.ratchet.RatchetPublicKey;
import im.actor.runtime.crypto.ratchet.RatchetRootChainKey;

public class EncryptedSessionChain {

    private PeerSession session;
    private byte[] ownPrivateKey;
    private byte[] theirPublicKey;
    private int sentCounter;
    private byte[] rootChainKey;

    public EncryptedSessionChain(PeerSession session, byte[] ownPrivateKey, byte[] theirPublicKey) {
        this.session = session;
        this.ownPrivateKey = ownPrivateKey;
        this.theirPublicKey = theirPublicKey;
        this.sentCounter = 0;
        this.rootChainKey = RatchetRootChainKey.makeRootChainKey(
                new RatchetPrivateKey(ownPrivateKey),
                new RatchetPublicKey(theirPublicKey),
                session.getMasterKey());
    }

    public PeerSession getSession() {
        return session;
    }

    public byte[] getOwnPrivateKey() {
        return ownPrivateKey;
    }

    public byte[] getTheirPublicKey() {
        return theirPublicKey;
    }

    public byte[] decrypt(byte[] data) throws IntegrityException {

        if (data.length < 80) {
            throw new IntegrityException("Data length is too small");
        }

        //
        // Parsing message header
        //

        final long senderEphermalKey0Id = ByteStrings.bytesToLong(data, 0);
        final long receiverEphermalKey0Id = ByteStrings.bytesToLong(data, 8);
        final byte[] senderEphemeralKey = ByteStrings.substring(data, 16, 32);
        final byte[] receiverEphemeralKey = ByteStrings.substring(data, 48, 32);
        final int messageIndex = ByteStrings.bytesToInt(data, 80);

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
        byte[] header = ByteStrings.substring(data, 0, 80);
        byte[] message = ByteStrings.substring(data, 80, data.length - 80);
        return ActorBox.openBox(header, message, ratchetMessageKey);
    }

    public byte[] encrypt(byte[] data) throws IntegrityException {
        int messageIndex = sentCounter++;
        ActorBoxKey ratchetMessageKey = RatchetMessageKey.buildKey(rootChainKey, messageIndex);

        byte[] header = ByteStrings.merge(
                ByteStrings.longToBytes(session.getOwnPreKeyId()), /*Alice Initial Ephermal*/
                ByteStrings.longToBytes(session.getTheirPreKeyId()), /*Bob Initial Ephermal*/
                Curve25519.keyGenPublic(ownPrivateKey),
                theirPublicKey,
                ByteStrings.intToBytes(messageIndex)); /* Message Index */

//        Log.d("EncryptedSessionChain#" + session.getPeerKeyGroupId(), "Own ephemeral Key: " + Crypto.keyHash(Curve25519.keyGenPublic(ownPrivateKey)));
//        Log.d("EncryptedSessionChain#" + session.getPeerKeyGroupId(), "Their ephemeral Key: " + Crypto.keyHash(theirPublicKey));

        return ByteStrings.merge(header, ActorBox.closeBox(header, data, Crypto.randomBytes(32), ratchetMessageKey));
    }

    public void safeErase() {
        for (int i = 0; i < ownPrivateKey.length; i++) {
            ownPrivateKey[i] = (byte) RandomUtils.randomId(255);
        }
        for (int i = 0; i < theirPublicKey.length; i++) {
            theirPublicKey[i] = (byte) RandomUtils.randomId(255);
        }
        for (int i = 0; i < rootChainKey.length; i++) {
            rootChainKey[i] = (byte) RandomUtils.randomId(255);
        }
        sentCounter = 0;
    }
}