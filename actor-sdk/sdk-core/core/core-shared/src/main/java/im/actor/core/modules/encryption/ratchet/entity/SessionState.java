package im.actor.core.modules.encryption.ratchet.entity;

import java.io.IOException;

import im.actor.runtime.Crypto;
import im.actor.runtime.bser.Bser;
import im.actor.runtime.bser.BserObject;
import im.actor.runtime.bser.BserValues;
import im.actor.runtime.bser.BserWriter;
import im.actor.runtime.crypto.Curve25519;

public class SessionState extends BserObject {

    public static SessionState fromBytes(byte[] data) throws IOException {
        return Bser.parse(new SessionState(), data);
    }

    private byte[] prevOwnPrivateKey;
    private byte[] prevOwnPublicKey;
    private byte[] latestOwnPrivateKey;
    private byte[] latestOwnPublicKey;
    private byte[] latestTheirKey;

    public SessionState(byte[] prevOwnPrivateKey, byte[] prevOwnPublicKey,
                        byte[] latestOwnPrivateKey, byte[] latestOwnPublicKey,
                        byte[] latestTheirKey) {
        this.prevOwnPrivateKey = prevOwnPrivateKey;
        this.prevOwnPublicKey = prevOwnPublicKey;
        this.latestOwnPrivateKey = latestOwnPrivateKey;
        this.latestOwnPublicKey = latestOwnPublicKey;
        this.latestTheirKey = latestTheirKey;
    }

    public SessionState() {

    }

    public byte[] getPrevOwnPrivateKey() {
        return prevOwnPrivateKey;
    }

    public byte[] getPrevOwnPublicKey() {
        return prevOwnPublicKey;
    }

    public byte[] getLatestOwnPrivateKey() {
        return latestOwnPrivateKey;
    }

    public byte[] getLatestOwnPublicKey() {
        return latestOwnPublicKey;
    }

    public byte[] getLatestTheirKey() {
        return latestTheirKey;
    }

    public SessionState updateKeys(byte[] theirKey) {
        byte[] nPrivate = Curve25519.keyGenPrivate(Crypto.randomBytes(32));
        byte[] nPublic = Curve25519.keyGenPublic(nPrivate);
        return new SessionState(latestOwnPrivateKey, latestOwnPublicKey, nPrivate, nPublic,
                theirKey);
    }

    @Override
    public void parse(BserValues values) throws IOException {
        prevOwnPrivateKey = values.optBytes(1);
        prevOwnPublicKey = values.optBytes(2);
        latestOwnPrivateKey = values.optBytes(3);
        latestOwnPublicKey = values.optBytes(4);
        latestTheirKey = values.optBytes(5);
    }

    @Override
    public void serialize(BserWriter writer) throws IOException {
        if (prevOwnPrivateKey != null) {
            writer.writeBytes(1, prevOwnPrivateKey);
        }
        if (prevOwnPublicKey != null) {
            writer.writeBytes(2, prevOwnPublicKey);
        }
        if (latestOwnPrivateKey != null) {
            writer.writeBytes(3, latestOwnPrivateKey);
        }
        if (latestOwnPublicKey != null) {
            writer.writeBytes(4, latestOwnPublicKey);
        }
        if (latestTheirKey != null) {
            writer.writeBytes(5, latestTheirKey);
        }
    }
}