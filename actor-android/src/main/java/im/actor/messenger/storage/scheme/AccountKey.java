package im.actor.messenger.storage.scheme;

import com.droidkit.bser.BserObject;
import com.droidkit.bser.BserValues;
import com.droidkit.bser.BserWriter;

import java.io.IOException;

/**
 * Created by ex3ndr on 15.11.14.
 */
public class AccountKey extends BserObject {
    private byte[] privateKey;
    private byte[] publicKey;

    public AccountKey(byte[] publicKey, byte[] privateKey) {
        this.privateKey = privateKey;
        this.publicKey = publicKey;
    }

    public AccountKey() {
    }

    public byte[] getPrivateKey() {
        return privateKey;
    }

    public byte[] getPublicKey() {
        return publicKey;
    }

    @Override
    public void parse(BserValues values) throws IOException {
        publicKey = values.optBytes(1);
        privateKey = values.optBytes(2);
    }

    @Override
    public void serialize(BserWriter writer) throws IOException {
        if (publicKey != null) {
            writer.writeBytes(1, publicKey);
        }

        if (privateKey != null) {
            writer.writeBytes(2, privateKey);
        }
    }
}
