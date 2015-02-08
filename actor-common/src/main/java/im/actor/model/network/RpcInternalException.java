package im.actor.model.network;

/**
 * Created by ex3ndr on 08.02.15.
 */
public class RpcInternalException extends RpcException {
    public RpcInternalException() {
        super("INTERNAL_ERROR", 500, "Internal server error", null);
    }
}
