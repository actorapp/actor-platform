package im.actor.model.network;

import im.actor.model.network.parser.Response;

/**
 * Created by ex3ndr on 08.02.15.
 */
public interface RpcCallback<T extends Response> {
    public void onResult(T response);

    public void onError(RpcException e);
}
