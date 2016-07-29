package im.actor.core.modules.encryption;

import org.jetbrains.annotations.NotNull;

import im.actor.core.api.ApiEncryptedBox;
import im.actor.core.api.ApiEncryptedContent;
import im.actor.core.api.ApiEncryptionKeyGroup;
import im.actor.core.modules.ModuleContext;
import im.actor.runtime.actors.ActorInterface;
import im.actor.runtime.actors.messages.Void;
import im.actor.runtime.promise.Promise;

import static im.actor.runtime.actors.ActorSystem.system;

public class EncryptedRouter extends ActorInterface {

    public EncryptedRouter(ModuleContext context) {
        super(system().actorOf("encryption/router", () -> new EncryptedRouterActor(context)));
    }

    public Promise<Void> onKeyGroupAdded(int uid, ApiEncryptionKeyGroup group) {
        return ask(new EncryptedRouterActor.KeyGroupAdded(uid, group));
    }

    public Promise<Void> onKeyGroupRemoved(int uid, int keyGroupId) {
        return ask(new EncryptedRouterActor.KeyGroupRemoved(uid, keyGroupId));
    }

    public Promise<Void> onEncryptedUpdate(int uid, long date, ApiEncryptedContent update) {
        return ask(new EncryptedRouterActor.EncryptedUpdate(uid, date, update));
    }

    public Promise<Void> onEncryptedBox(long date, int senderId, @NotNull ApiEncryptedBox encryptedBox) {
        return ask(new EncryptedRouterActor.EncryptedPackageUpdate(date, senderId, encryptedBox));
    }
}
