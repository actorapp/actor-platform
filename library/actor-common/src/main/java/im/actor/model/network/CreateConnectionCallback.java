/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.model.network;

public interface CreateConnectionCallback {
    void onConnectionCreated(Connection connection);

    void onConnectionCreateError();
}
