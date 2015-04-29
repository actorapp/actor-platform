package im.actor.model.js.providers.websocket;

/**
 * Created by ex3ndr on 29.04.15.
 */
public interface AsyncConnectionInterface {

    public void onConnected();

    public void onReceived(byte[] data);

    public void onClosed();
}