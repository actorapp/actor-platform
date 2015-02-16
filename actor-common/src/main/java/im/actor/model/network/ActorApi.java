package im.actor.model.network;

import im.actor.model.Networking;
import im.actor.model.droidkit.actors.ActorRef;
import im.actor.model.network.api.ApiBroker;
import im.actor.model.network.parser.Request;
import im.actor.model.network.parser.Response;

/**
 * Created by ex3ndr on 08.02.15.
 */
public class ActorApi {
    private ActorRef apiBroker;

    public ActorApi(Endpoints endpoints, AuthKeyStorage keyStorage, ActorApiCallback callback,
                    Networking networking) {
        this.apiBroker = ApiBroker.get(endpoints, keyStorage, callback, networking);
    }

    public <T extends Response> void request(Request<T> request, RpcCallback<T> callback) {
        this.apiBroker.send(new ApiBroker.PerformRequest(request, callback));
    }
}
