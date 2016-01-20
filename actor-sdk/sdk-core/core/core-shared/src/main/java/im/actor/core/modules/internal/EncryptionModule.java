package im.actor.core.modules.internal;

import java.util.HashMap;

import im.actor.core.modules.AbsModule;
import im.actor.core.modules.ModuleContext;
import im.actor.core.modules.internal.encryption.EncryptedStateActor;
import im.actor.core.modules.internal.encryption.KeyManagerActor;
import im.actor.runtime.actors.Actor;
import im.actor.runtime.actors.ActorCreator;
import im.actor.runtime.actors.ActorRef;
import im.actor.runtime.actors.Props;

import static im.actor.runtime.actors.ActorSystem.system;

public class EncryptionModule extends AbsModule {

    private ActorRef keyManager;
    private HashMap<Integer, ActorRef> encryptedStates = new HashMap<Integer, ActorRef>();

    public EncryptionModule(ModuleContext context) {
        super(context);
    }

    public void run() {
        keyManager = system().actorOf(Props.create(KeyManagerActor.class,
                new ActorCreator<KeyManagerActor>() {
                    @Override
                    public KeyManagerActor create() {
                        return new KeyManagerActor(context());
                    }
                }), "encryption/keys");
    }

    public ActorRef getKeyManager() {
        return keyManager;
    }

    public ActorRef getEncryptedChatManager(final int uid) {
        synchronized (encryptedStates) {
            if (!encryptedStates.containsKey(uid)) {
                encryptedStates.put(uid, system().actorOf(Props.create(EncryptedStateActor.class, new ActorCreator<EncryptedStateActor>() {
                    @Override
                    public EncryptedStateActor create() {
                        return new EncryptedStateActor(uid, context());
                    }
                }), "encryption/uid_" + uid));
            }
            return encryptedStates.get(uid);
        }
    }
}
