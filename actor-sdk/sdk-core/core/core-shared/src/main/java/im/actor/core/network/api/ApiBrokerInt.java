package im.actor.core.network.api;

import im.actor.core.network.ActorApiCallback;
import im.actor.core.network.AuthKeyStorage;
import im.actor.core.network.Endpoints;
import im.actor.runtime.actors.ActorInterface;
import im.actor.runtime.promise.Promise;

import static im.actor.runtime.actors.ActorSystem.system;

public class ApiBrokerInt extends ActorInterface {
    public ApiBrokerInt(final Endpoints endpoints, final AuthKeyStorage keyStorage, final ActorApiCallback callback,
                        final boolean isEnableLog, int id, final int minDelay,
                        final int maxDelay,
                        final int maxFailureCount) {
        setDest(system().actorOf("api/broker#" + id, () -> new ApiBroker(endpoints,
                keyStorage,
                callback,
                isEnableLog,
                minDelay,
                maxDelay,
                maxFailureCount)));
    }

    public Promise<Boolean> checkIsCurrentAuthId(long authId) {
        return ask(new CheckIsCurrentAuthId(authId));
    }


}
