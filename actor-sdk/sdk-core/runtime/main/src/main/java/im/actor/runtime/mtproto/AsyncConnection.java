/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.runtime.mtproto;

import com.google.j2objc.annotations.ObjectiveCName;

public abstract class AsyncConnection {

    private AsyncConnectionInterface connection;
    private ConnectionEndpoint endpoint;

    @ObjectiveCName("initWithEndpoint:withInterface:")
    public AsyncConnection(ConnectionEndpoint endpoint, AsyncConnectionInterface connection) {
        this.connection = connection;
        this.endpoint = endpoint;
    }

    @ObjectiveCName("doConnect")
    public abstract void doConnect();

    @ObjectiveCName("doSend:")
    public abstract void doSend(byte[] data);

    @ObjectiveCName("doClose")
    public abstract void doClose();

    @ObjectiveCName("getEndpoint")
    protected ConnectionEndpoint getEndpoint() {
        return endpoint;
    }

    @ObjectiveCName("onConnected")
    protected final void onConnected() {
        connection.onConnected();
    }

    @ObjectiveCName("onReceived:")
    protected final void onReceived(byte[] data) {
        connection.onReceived(data);
    }

    @ObjectiveCName("onClosed")
    protected final void onClosed() {
        connection.onClosed();
    }
}
