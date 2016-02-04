package im.actor.core.modules.encryption;

import im.actor.core.api.ApiEncryptionKeyGroup;
import im.actor.core.modules.ModuleContext;
import im.actor.core.modules.encryption.entity.PrivateKey;
import im.actor.core.modules.encryption.entity.PublicKey;
import im.actor.core.modules.encryption.entity.UserKeys;
import im.actor.runtime.actors.ActorInterface;
import im.actor.runtime.actors.ActorRef;
import im.actor.runtime.function.Function;
import im.actor.runtime.function.Supplier;
import im.actor.runtime.promise.Promise;
import im.actor.runtime.promise.Promises;

import static im.actor.runtime.actors.ActorSystem.system;

public class KeyManagerInt extends ActorInterface {

    public KeyManagerInt(ModuleContext context) {
        super(system().actorOf("encryption/keys", KeyManagerActor.CONSTRUCTOR(context)));
    }

    public Promise<KeyManagerActor.OwnIdentity> getOwnIdentity() {
        return ask(new KeyManagerActor.FetchOwnKey());
    }

    public Promise<UserKeys> getUserKeyGroups(int uid) {
        return ask(new KeyManagerActor.FetchUserKeys(uid));
    }

    public Promise<PublicKey> getUserRandomPreKey(int uid, int keyGroupId) {
        return ask(new KeyManagerActor.FetchUserPreKeyRandom(uid, keyGroupId));
    }

    public Promise<PublicKey> getUserPreKey(int uid, int keyGroupId, long preKeyId) {
        return ask(new KeyManagerActor.FetchUserPreKey(uid, keyGroupId, preKeyId));
    }

    public Promise<PrivateKey> getOwnRandomPreKey() {
        return ask(new KeyManagerActor.FetchOwnRandomPreKey());
    }

    public Promise<PrivateKey> getOwnPreKey(long id) {
        return ask(new KeyManagerActor.FetchOwnPreKeyById(id));
    }

    public Promise<PrivateKey> getOwnPreKey(byte[] publicKey) {
        return ask(new KeyManagerActor.FetchOwnPreKeyByPublic(publicKey));
    }

    public void onKeyGroupsAdded(int uid, ApiEncryptionKeyGroup keyGroup) {
        send(new KeyManagerActor.PublicKeysGroupAdded(uid, keyGroup));
    }

    public void onKeyGroupsRemoved(int uid, int keyGroupId) {
        send(new KeyManagerActor.PublicKeysGroupRemoved(uid, keyGroupId));
    }

    public Supplier<Promise<byte[]>> supplyUserPreKey(final int uid, final int keyGroupId) {
        return new Supplier<Promise<byte[]>>() {
            @Override
            public Promise<byte[]> get() {
                return getUserRandomPreKey(uid, keyGroupId)
                        .map(new Function<PublicKey, byte[]>() {
                            @Override
                            public byte[] apply(PublicKey publicKey) {
                                return publicKey.getPublicKey();
                            }
                        });
            }
        };
    }
}
