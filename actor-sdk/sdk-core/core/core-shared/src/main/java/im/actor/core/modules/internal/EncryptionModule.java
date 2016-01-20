package im.actor.core.modules.internal;

import im.actor.core.modules.AbsModule;
import im.actor.core.modules.ModuleContext;
import im.actor.core.modules.internal.encryption.KeyManagerActor;
import im.actor.runtime.actors.ActorCreator;
import im.actor.runtime.actors.ActorRef;
import im.actor.runtime.actors.Props;

import static im.actor.runtime.actors.ActorSystem.system;

public class EncryptionModule extends AbsModule {

    private ActorRef keyManager;

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
}
