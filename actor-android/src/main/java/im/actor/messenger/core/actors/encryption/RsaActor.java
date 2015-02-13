package im.actor.messenger.core.actors.encryption;

// import com.crashlytics.android.Crashlytics;
import com.droidkit.actors.ActorSystem;
import com.droidkit.actors.Props;
import com.droidkit.actors.concurrency.Future;
import com.droidkit.actors.typed.TypedActor;
import com.droidkit.actors.typed.TypedCreator;

import im.actor.api.crypto.KeyTools;
import im.actor.messenger.core.encryption.RsaEncryptionUtils;

import javax.crypto.Cipher;

import java.security.PublicKey;
import java.util.ArrayList;

/**
 * Created by ex3ndr on 14.09.14.
 */
public class RsaActor extends TypedActor<RsaEncryptor> implements RsaEncryptor {

    private static final Object LOCK = new Object();

    private static RsaEncryptor encryptor;

    public static RsaEncryptor encryptor() {
        if (encryptor == null) {
            synchronized (LOCK) {
                if (encryptor == null) {
                    encryptor = TypedCreator.typed(
                            ActorSystem.system().actorOf(Props.create(RsaActor.class)
                                    .changeDispatcher("rsa"), "rsa"),
                            RsaEncryptor.class);
                }
            }
        }
        return encryptor;
    }

    public RsaActor() {
        super(RsaEncryptor.class);
    }

    private Cipher cipher;

    @Override
    public void preStart() {
        super.preStart();
        cipher = RsaEncryptionUtils.createRSACipher();
    }

    @Override
    public Future<RsaResult> encrypt(byte[] data, im.actor.messenger.storage.scheme.users.PublicKey[] myKeys,
                                     im.actor.messenger.storage.scheme.users.PublicKey[] foreign) {
        ArrayList<RsaResult.RsaPart> myParts = new ArrayList<RsaResult.RsaPart>();
        ArrayList<RsaResult.RsaPart> foreignParts = new ArrayList<RsaResult.RsaPart>();

        for (im.actor.messenger.storage.scheme.users.PublicKey k : myKeys) {
            PublicKey pk = KeyTools.decodeRsaPublicKey(k.getKey());

            try {
                cipher.init(Cipher.ENCRYPT_MODE, pk);
                byte[] encrypted = cipher.doFinal(data);
                myParts.add(new RsaResult.RsaPart(encrypted, k.getKeyHash()));
            } catch (Exception e) {
                e.printStackTrace();
                // Crashlytics.logException(e);
                myParts.add(new RsaResult.RsaPart(k.getKeyHash()));
            }
        }

        for (im.actor.messenger.storage.scheme.users.PublicKey k : foreign) {
            PublicKey pk = KeyTools.decodeRsaPublicKey(k.getKey());
            try {
                cipher.init(Cipher.ENCRYPT_MODE, pk);
                byte[] encrypted = cipher.doFinal(data);
                foreignParts.add(new RsaResult.RsaPart(encrypted, k.getKeyHash()));
            } catch (Exception e) {
                e.printStackTrace();
                // Crashlytics.logException(e);
                foreignParts.add(new RsaResult.RsaPart(k.getKeyHash()));
            }
        }

        return result(new RsaResult(myParts.toArray(new RsaResult.RsaPart[0]), foreignParts.toArray(new RsaResult.RsaPart[0])));
    }
}
