package im.actor.model.network;

/**
 * Created by ex3ndr on 08.02.15.
 */
public class RpcTimeoutException extends RpcException {
    public RpcTimeoutException() {
        super("TIMEOUT", 500, "Request timeout", true, null);
    }
}
