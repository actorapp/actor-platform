package im.actor.model.network;

/**
* Created by ex3ndr on 16.02.15.
*/
public interface CreateConnectionCallback {
    public void onConnectionCreated(Connection connection);

    public void onConnectionCreateError();
}
