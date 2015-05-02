package im.actor.model.network.connection;

/**
 * Created by ex3ndr on 29.04.15.
 */
public interface ManagedConnectionCreateCallback {
    public void onConnectionCreated(ManagedConnection connection);

    public void onConnectionCreateError(ManagedConnection connection);
}
