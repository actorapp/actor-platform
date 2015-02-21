package im.actor.gwt.app.websocket;

import com.google.gwt.typedarrays.shared.TypedArrays;
import com.google.gwt.typedarrays.shared.Uint8Array;

import im.actor.model.network.Connection;
import im.actor.model.network.ConnectionCallback;
import im.actor.model.network.CreateConnectionCallback;

/**
 * Created by ex3ndr on 07.02.15.
 */
public class WebSocketConnection implements Connection {

    private boolean isCreated = false;
    private boolean isClosed = false;

    private WebSocket webSocket;

    public WebSocketConnection(String url, final ConnectionCallback callback,
                               final CreateConnectionCallback factoryCallback) {
        webSocket = new WebSocket(url, new WebSocketCallback() {
            @Override
            public void onOpen() {
                if (!isCreated) {
                    isCreated = true;
                    factoryCallback.onConnectionCreated(WebSocketConnection.this);
                } else {
                    // Just ignore this
                }
            }

            @Override
            public void onClose() {
                if (!isCreated) {
                    isCreated = true;
                    factoryCallback.onConnectionCreateError();
                } else {
                    callback.onConnectionDie();
                }
            }

            @Override
            public void onMessage(byte[] message) {
                if (isCreated && !isClosed) {
                    callback.onMessage(message, 0, message.length);
                }
            }
        });
    }

    @Override
    public void post(byte[] data, int offset, int len) {
        if (isClosed || !isCreated) {
            return;
        }
        Uint8Array push = TypedArrays.createUint8Array(len);
        for (int i = offset; i < offset + len; i++) {
            push.set(i - offset, data[i]);
        }
        webSocket.send(push);
    }

    @Override
    public boolean isClosed() {
        return isClosed;
    }

    @Override
    public void close() {
        if (!isClosed) {
            isClosed = true;
            webSocket.close();
        }
    }
}
