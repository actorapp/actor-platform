package im.actor.core.modules.encryption;

import im.actor.runtime.Log;
import im.actor.runtime.actors.ActorInterface;
import im.actor.runtime.actors.ActorRef;
import im.actor.runtime.function.Consumer;
import im.actor.runtime.function.Map;
import im.actor.runtime.promise.Promise;
import im.actor.runtime.promise.Promises;

public class KeyManagerInt extends ActorInterface {

    public KeyManagerInt(ActorRef dest) {
        super(dest);
    }

    public Promise<KeyManagerActor.FetchOwnKeyGroupResult> getOwnGroup() {
        return ask(new KeyManagerActor.FetchOwnKeyGroup());
    }

    public Promise<KeyManagerActor.FetchUserKeyGroupsResponse> getUserKeyGroups(int uid) {
        return ask(new KeyManagerActor.FetchUserKeyGroups(uid));
    }

    public Promise<KeyManagerActor.FetchUserEphemeralKeyResponse> getUserRandomPreKey(int uid, int keyGroupId) {
        return ask(new KeyManagerActor.FetchUserEphemeralKeyRandom(uid, keyGroupId));
    }

    public Promise<KeyManagerActor.FetchOwnEphemeralKeyResult> getOwnRandomPreKey() {
        return ask(new KeyManagerActor.FetchOwnEphemeralKey());
    }

    public Promise<byte[]> getEphemeralKey(byte[] defaultVal, int uid, int keyGroupId) {
        return Promises.success(defaultVal)
                .mapPromise(new Map<byte[], Promise<byte[]>>() {
                    @Override
                    public Promise<byte[]> map(byte[] src) {
                        if (src != null) {
                            return Promises.success(src);
                        }

                        return getUserRandomPreKey(uid, keyGroupId)
                                .map(src1 -> src1.getEphemeralKey().getPublicKey());
                    }
                });
    }
}
