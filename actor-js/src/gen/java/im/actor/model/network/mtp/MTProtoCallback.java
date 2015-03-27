package im.actor.model.network.mtp;

/**
 * Created by ex3ndr on 08.02.15.
 */
public interface MTProtoCallback {
    public void onRpcResponse(long mid, byte[] content);

    public void onUpdate(byte[] content);

    public void onAuthKeyInvalidated(long authKey);

    public void onSessionCreated();
}
