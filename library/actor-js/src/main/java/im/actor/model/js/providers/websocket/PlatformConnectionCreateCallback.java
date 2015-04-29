package im.actor.model.js.providers.websocket;

import im.actor.model.network.Connection;

/**
 * Created by ex3ndr on 29.04.15.
 */
public interface PlatformConnectionCreateCallback {
    public void onConnectionCreated(PlatformConnection connection);

    public void onConnectionCreateError(PlatformConnection connection);
}
