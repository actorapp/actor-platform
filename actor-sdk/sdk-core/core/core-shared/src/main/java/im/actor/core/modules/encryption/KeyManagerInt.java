package im.actor.core.modules.encryption;

import im.actor.core.modules.encryption.entity.PublicKey;
import im.actor.runtime.actors.ActorInterface;
import im.actor.runtime.actors.ActorRef;
import im.actor.runtime.function.Function;
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

    public Promise<PublicKey> getUserRandomPreKey(int uid, int keyGroupId) {
        return ask(new KeyManagerActor.FetchUserEphemeralKeyRandom(uid, keyGroupId))
                .map(new Function<KeyManagerActor.FetchUserEphemeralKeyResponse, PublicKey>() {
                    @Override
                    public PublicKey apply(KeyManagerActor.FetchUserEphemeralKeyResponse fetchUserEphemeralKeyResponse) {
                        return fetchUserEphemeralKeyResponse.getEphemeralKey();
                    }
                });
    }

    public Promise<KeyManagerActor.FetchOwnEphemeralKeyResult> getOwnRandomPreKey() {
        return ask(new KeyManagerActor.FetchOwnEphemeralKey());
    }

    public Promise<byte[]> getEphemeralKey(byte[] defaultVal, final int uid, final int keyGroupId) {
        return Promises.success(defaultVal)
                .mapPromise(new Function<byte[], Promise<byte[]>>() {
                    @Override
                    public Promise<byte[]> apply(byte[] src) {
                        if (src != null) {
                            return Promises.success(src);
                        }

                        return getUserRandomPreKey(uid, keyGroupId)
                                .map(new Function<PublicKey, byte[]>() {
                                    @Override
                                    public byte[] apply(PublicKey publicKey) {
                                        return publicKey.getPublicKey();
                                    }
                                });
                    }
                });
    }
}
