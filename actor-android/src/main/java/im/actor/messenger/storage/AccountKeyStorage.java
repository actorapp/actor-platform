package im.actor.messenger.storage;

import android.content.Context;

import com.droidkit.bser.Bser;

import java.io.IOException;
import java.security.KeyPair;

import im.actor.api.crypto.KeyTools;
import im.actor.messenger.storage.scheme.AccountKey;
import im.actor.messenger.util.io.SafeFileWriter;

/**
 * Created by ex3ndr on 15.11.14.
 */
public class AccountKeyStorage {

    private SafeFileWriter fileWriter;
    private AccountKey key;
    private KeyPair keyPair;

    public AccountKeyStorage(Context context) {
        fileWriter = new SafeFileWriter(context, "key.bin");
        byte[] data = fileWriter.loadData();
        if (data != null && data.length > 0) {
            try {
                key = Bser.parse(AccountKey.class, data);
                if (key.getPrivateKey() != null) {
                    keyPair = new KeyPair(
                            KeyTools.decodeRsaPublicKey(key.getPublicKey()),
                            KeyTools.decodeRsaPrivateKey(key.getPrivateKey()));
                }
            } catch (IOException e) {
                e.printStackTrace();
                key = new AccountKey();
            }
        }

        if (key == null) {
            key = new AccountKey();
        }
    }

    public KeyPair getKeyPair() {
        return keyPair;
    }

    public void setKey(KeyPair pair) {
        this.key = new AccountKey(KeyTools.encodeRsaPublicKey(pair.getPublic()),
                KeyTools.encodeRsaPrivateKey(pair.getPrivate()));
        this.keyPair = pair;
        this.fileWriter.saveData(key.toByteArray());
    }
}
