package im.actor.core.modules.encryption;

import java.util.HashMap;

import im.actor.core.modules.AbsModule;
import im.actor.core.modules.ModuleContext;
import im.actor.runtime.Storage;
import im.actor.runtime.actors.ActorCreator;
import im.actor.runtime.actors.ActorRef;
import im.actor.runtime.actors.Props;
import im.actor.runtime.storage.KeyValueStorage;

import static im.actor.runtime.actors.ActorSystem.system;

public class EncryptionModule extends AbsModule {

    private KeyManagerInt keyManagerInt;
    private SessionManagerInt sessionManagerInt;
    private EncryptedInt encryptedInt;
    private HashMap<Integer, ActorRef> encryptedStates = new HashMap<>();
    private KeyValueStorage sessionStorage;

    public EncryptionModule(ModuleContext context) {
        super(context);
    }

    public void run() {
        sessionStorage = Storage.createKeyValue("encryption_session_int");
        keyManagerInt = new KeyManagerInt(context());
        sessionManagerInt = new SessionManagerInt(context());
        encryptedInt = new EncryptedInt(context());
    }

    public KeyValueStorage getSessionStorage() {
        return sessionStorage;
    }

    public SessionManagerInt getSessionManagerInt() {
        return sessionManagerInt;
    }

    public EncryptedInt getEncrypted() {
        return encryptedInt;
    }

    public KeyManagerInt getKeyManagerInt() {
        return keyManagerInt;
    }

    public ActorRef getEncryptedChatManager(final int uid) {
        synchronized (encryptedStates) {
            if (!encryptedStates.containsKey(uid)) {
                encryptedStates.put(uid, system().actorOf(Props.create(new ActorCreator() {
                    @Override
                    public EncryptedPeerActor create() {
                        return new EncryptedPeerActor(uid, context());
                    }
                }), "encryption/uid_" + uid));
            }
            return encryptedStates.get(uid);
        }
    }
}
