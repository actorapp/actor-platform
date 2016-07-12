package im.actor.core.modules.encryption;

import java.util.HashMap;

import im.actor.core.api.ApiEncryptedBox;
import im.actor.core.api.ApiEncryptedMessage;
import im.actor.core.api.ApiMessage;
import im.actor.core.modules.AbsModule;
import im.actor.core.modules.ModuleContext;
import im.actor.core.modules.encryption.ratchet.EncryptedMsg;
import im.actor.core.modules.encryption.ratchet.EncryptedMsgActor;
import im.actor.core.modules.encryption.ratchet.EncryptedUser;
import im.actor.core.modules.encryption.ratchet.EncryptedUserActor;
import im.actor.core.modules.encryption.ratchet.KeyManager;
import im.actor.core.modules.encryption.ratchet.SessionManager;
import im.actor.core.network.mtp.entity.EncryptedPackage;
import im.actor.runtime.promise.Promise;

import static im.actor.runtime.actors.ActorSystem.system;

public class EncryptionModule extends AbsModule {

    private KeyManager keyManager;
    private SessionManager sessionManager;
    private EncryptedMsg encryption;

    private final HashMap<Integer, EncryptedUser> users = new HashMap<>();

    public EncryptionModule(ModuleContext context) {
        super(context);
    }

    public void run() {

        keyManager = new KeyManager(context());

        sessionManager = new SessionManager(context());

        encryption = new EncryptedMsg(system().actorOf("encryption/messaging",
                () -> new EncryptedMsgActor(context())));
    }

    public KeyManager getKeyManager() {
        return keyManager;
    }

    public SessionManager getSessionManager() {
        return sessionManager;
    }

    public EncryptedMsg getEncryption() {
        return encryption;
    }

    public EncryptedUser getEncryptedUser(int uid) {
        synchronized (users) {
            if (!users.containsKey(uid)) {
                users.put(uid, new EncryptedUser(system().actorOf("encryption/uid_" + uid,
                        () -> new EncryptedUserActor(uid, context()))));
            }
            return users.get(uid);
        }
    }

    public Promise<ApiEncryptedBox> encrypt(int uid, ApiMessage message) {
        return getEncryption().encrypt(uid, message);
    }
}
