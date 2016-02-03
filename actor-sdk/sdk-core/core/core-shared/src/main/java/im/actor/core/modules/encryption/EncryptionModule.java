package im.actor.core.modules.encryption;

import java.util.HashMap;

import im.actor.core.modules.AbsModule;
import im.actor.core.modules.ModuleContext;
import im.actor.runtime.actors.ActorCreator;
import im.actor.runtime.actors.ActorRef;
import im.actor.runtime.actors.Props;

import static im.actor.runtime.actors.ActorSystem.system;

public class EncryptionModule extends AbsModule {

    private ActorRef keyManager;
    private KeyManagerInt keyManagerInt;
    private ActorRef sessionManager;
    private SessionManagerInt sessionManagerInt;

    private ActorRef encrypted;
    private EncryptedInt encryptedInt;
    private HashMap<Integer, ActorRef> encryptedStates = new HashMap<Integer, ActorRef>();

    public EncryptionModule(ModuleContext context) {
        super(context);
    }

    public void run() {

        keyManager = system().actorOf("encryption/keys",
                KeyManagerActor.CONSTRUCTOR(context()));
        keyManagerInt = new KeyManagerInt(keyManager);

        sessionManager = system().actorOf("encryption/sessions",
                SessionManagerActor.CONSTRUCTOR(context()));
        sessionManagerInt = new SessionManagerInt(sessionManager);

        encrypted = system().actorOf("encryption/messaging",
                EncryptedActor.CONSTRUCTOR(context()));
        encryptedInt = new EncryptedInt(encrypted);
    }

    public SessionManagerInt getSessionManagerInt() {
        return sessionManagerInt;
    }

    public EncryptedInt getEncrypted() {
        return encryptedInt;
    }

    public ActorRef getKeyManager() {
        return keyManager;
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
