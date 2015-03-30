package im.actor.model.network;

/**
 * Created by ex3ndr on 06.02.15.
 */
public interface ConnectionCallback {
    public void onMessage(byte[] data, int offset, int len);

    public void onConnectionDie();
}
