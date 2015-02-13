package im.actor.messenger.core.actors.keys;

import com.droidkit.actors.ActorCreator;
import com.droidkit.actors.ActorSelection;
import com.droidkit.actors.Props;
import com.droidkit.actors.concurrency.FutureCallback;
import com.droidkit.actors.tasks.TaskActor;

import java.util.ArrayList;
import java.util.List;

import im.actor.api.scheme.PublicKeyRequest;
import im.actor.api.scheme.rpc.ResponseGetPublicKeys;
import im.actor.messenger.storage.scheme.users.PublicKey;
import im.actor.messenger.util.Logger;

import static im.actor.messenger.core.Core.requests;
import static im.actor.messenger.storage.KeyValueEngines.publicKeys;

/**
 * Created by ex3ndr on 02.09.14.
 */
public class KeyLoaderActor extends TaskActor<Void> {

    public static ActorSelection loader(final int uid, final long accessHash, final long keyHash) {
        return new ActorSelection(Props.create(KeyLoaderActor.class, new ActorCreator<KeyLoaderActor>() {
            @Override
            public KeyLoaderActor create() {
                return new KeyLoaderActor(uid, accessHash, keyHash);
            }
        }), "key_" + uid + "_" + keyHash);
    }

    private int uid;
    private long accessHash;
    private long keyHash;

    public KeyLoaderActor(int uid, long accessHash, long keyHash) {
        this.uid = uid;
        this.accessHash = accessHash;
        this.keyHash = keyHash;
    }

    @Override
    public void startTask() {
        Logger.d("KeyLoader", "Starring loadingKey " + uid + "@" + keyHash);
        List<PublicKeyRequest> keys = new ArrayList<PublicKeyRequest>();
        keys.add(new PublicKeyRequest(uid, accessHash, keyHash));
        ask(requests().getPublicKeys(keys), new FutureCallback<ResponseGetPublicKeys>() {
            @Override
            public void onResult(ResponseGetPublicKeys result) {
                Logger.d("KeyLoader", "Key loaded " + uid + "@" + keyHash);
                for (im.actor.api.scheme.PublicKey key : result.getKeys()) {
                    publicKeys().put(new PublicKey(key.getUid(), key.getKeyHash(), key.getKey()));
                }
                complete(null);
            }

            @Override
            public void onError(Throwable throwable) {
                Logger.d("KeyLoader", "Key load error " + uid + "@" + keyHash);
                error(null);
            }
        });
    }
}
