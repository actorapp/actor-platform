package im.actor.core.modules.encryption.session;

import java.util.HashSet;
import java.util.Random;

import im.actor.core.util.RandomUtils;
import im.actor.runtime.Crypto;
import im.actor.runtime.Log;
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

    public byte[] encrypt(byte[] data) throws IntegrityException {
        int messageIndex = sentCounter++;
        ActorBoxKey ratchetMessageKey = RatchetMessageKey.buildKey(rootChainKey, messageIndex);

        byte[] header = ByteStrings.merge(
                ByteStrings.intToBytes(session.getPeerKeyGroupId()),
                ByteStrings.longToBytes(session.getOwnPreKey().getKeyId()), /*Alice Initial Ephermal*/
                ByteStrings.longToBytes(session.getTheirPreKey().getKeyId()), /*Bob Initial Ephermal*/
                Curve25519.keyGenPublic(ownPrivateKey),
                theirPublicKey,
                ByteStrings.intToBytes(messageIndex)); /* Message Index */

        Log.d("EncryptedSessionChain", "Own ephemeral Key: " + Crypto.keyHash(Curve25519.keyGenPublic(ownPrivateKey)));
        Log.d("EncryptedSessionChain", "Their ephemeral Key: " + Crypto.keyHash(theirPublicKey));

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
        receivedCounters.clear();
        sentCounter = 0;
    }
}