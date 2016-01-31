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

    private ActorRef keyManager;
    private KeyManagerInt keyManagerInt;
    private ActorRef sessionManager;
    private SessionManagerInt sessionManagerInt;

    private ActorRef messageEncryptor;
    private HashMap<Integer, ActorRef> encryptedStates = new HashMap<Integer, ActorRef>();

    public EncryptionModule(ModuleContext context) {
        super(context);
    }

    public void run() {
        keyManager = system().actorOf(Props.create(KeyManagerActor.class, new ActorCreator<KeyManagerActor>() {
            @Override
            public KeyManagerActor create() {
                return new KeyManagerActor(context());
            }
        }), "encryption/keys");
        keyManagerInt = new KeyManagerInt(keyManager);
        sessionManager = system().actorOf(Props.create(SessionManagerActor.class, new ActorCreator<SessionManagerActor>() {
            @Override
            public SessionManagerActor create() {
                return new SessionManagerActor(context());
            }
        }), "encryption/sessions");
        sessionManagerInt = new SessionManagerInt(sessionManager);
        messageEncryptor = system().actorOf(Props.create(EncryptedMsgActor.class, new ActorCreator<EncryptedMsgActor>() {
            @Override
            public EncryptedMsgActor create() {
                return new EncryptedMsgActor(context());
            }
        }), "encryption/messaging");
    }

    public SessionManagerInt getSessionManagerInt() {
        return sessionManagerInt;
    }

    public ActorRef getMessageEncryptor() {
        return messageEncryptor;
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
                encryptedStates.put(uid, system().actorOf(Props.create(EncryptedPeerActor.class, new ActorCreator<EncryptedPeerActor>() {
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
