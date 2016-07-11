package im.actor.core.modules.encryption;

import java.util.HashMap;

import im.actor.core.modules.AbsModule;
import im.actor.core.modules.ModuleContext;
import im.actor.core.modules.encryption.EncryptedPeerActor;
import im.actor.core.modules.encryption.KeyManagerActor;
import im.actor.core.modules.encryption.EncryptedMsgActor;
import im.actor.core.modules.encryption.KeyManagerInt;
import im.actor.runtime.actors.ActorCreator;
import im.actor.runtime.actors.ActorRef;
import im.actor.runtime.actors.Props;

import static im.actor.runtime.actors.ActorSystem.system;

public class EncryptionModule extends AbsModule {

    private KeyManagerInt keyManagerInt;
    private SessionManagerInt sessionManagerInt;

    private ActorRef messageEncryptor;
    private final HashMap<Integer, ActorRef> encryptedStates = new HashMap<>();

    public EncryptionModule(ModuleContext context) {
        super(context);
    }

    public void run() {

        keyManagerInt = new KeyManagerInt(context());

        // Session Manager
        ActorRef sessionManager = system().actorOf("encryption/sessions",
                () -> new SessionManagerActor(context()));
        sessionManagerInt = new SessionManagerInt(sessionManager);


        messageEncryptor = system().actorOf("encryption/messaging",
                () -> new EncryptedMsgActor(context()));
    }

    public SessionManagerInt getSessionManager() {
        return sessionManagerInt;
    }

    public KeyManagerInt getKeyManager() {
        return keyManagerInt;
    }


    public ActorRef getMessageEncryptor() {
        return messageEncryptor;
    }

    public ActorRef getEncryptedChatManager(final int uid) {
        synchronized (encryptedStates) {
            if (!encryptedStates.containsKey(uid)) {
                encryptedStates.put(uid, system().actorOf("encryption/uid_" + uid,
                        () -> new EncryptedPeerActor(uid, context())));
            }
            return encryptedStates.get(uid);
        }
    }
}
