package im.actor.model.network;

/**
 * Created by ex3ndr on 08.02.15.
 */
public interface ActorApiCallback {
    public void onAuthIdInvalidated(long authKey);

    public void onNewSessionCreated();

    public void onUpdateReceived(Object obj);
}