package im.actor.model.network;

/**
 * Created by ex3ndr on 06.02.15.
 */
public interface ConnectionCallback {

    void onConnectionRedirect(String host, int port, int timeout);

    void onMessage(byte[] data, int offset, int len);

    void onConnectionDie();
}
