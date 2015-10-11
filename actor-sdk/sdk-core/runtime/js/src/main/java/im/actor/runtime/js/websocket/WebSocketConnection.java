/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.runtime.js.websocket;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.typedarrays.shared.ArrayBuffer;
import com.google.gwt.typedarrays.shared.TypedArrays;
import com.google.gwt.typedarrays.shared.Uint8Array;

import im.actor.runtime.js.utils.Conversion;
import im.actor.runtime.mtproto.AsyncConnection;
import im.actor.runtime.mtproto.AsyncConnectionInterface;
import im.actor.runtime.mtproto.ConnectionEndpoint;

public class WebSocketConnection extends AsyncConnection {

    private JavaScriptObject jsWebSocket;
    private boolean isClosed;

    public WebSocketConnection(ConnectionEndpoint endpoint, AsyncConnectionInterface connection) {
        super(endpoint, connection);
    }

    @Override
    public void doConnect() {
        isClosed = true;

        String url;
        if (getEndpoint().getType() == ConnectionEndpoint.Type.WS) {
            url = "ws://" + getEndpoint().getHost() + ":" + getEndpoint().getPort() + "/";
        } else if (getEndpoint().getType() == ConnectionEndpoint.Type.WS_TLS) {
            url = "wss://" + getEndpoint().getHost() + ":" + getEndpoint().getPort() + "/";
        } else {
            throw new RuntimeException();
        }
        this.jsWebSocket = createJSWebSocket(url, this);
    }

    @Override
    public void doSend(byte[] data) {
        if (isClosed) {
            return;
        }
        Uint8Array push = TypedArrays.createUint8Array(data.length);
        for (int i = 0; i < data.length; i++) {
            push.set(i, data[i]);
        }
        send(push);
    }

    @Override
    public void doClose() {
        isClosed = true;
        close();
    }

    private void onRawMessage(ArrayBuffer message) {
        if (isClosed) {
            return;
        }
        onReceived(Conversion.convertBytes(message));
    }

    private void onRawConnected() {
        isClosed = false;
        onConnected();
    }

    private void onRawClosed() {
        isClosed = true;
        onClosed();
    }

    // Native interfaces

    public native void send(Uint8Array message) /*-{
        if (message == null)
            return;

        this.@im.actor.runtime.js.websocket.WebSocketConnection::jsWebSocket.send(message);
    }-*/;

    public native void close() /*-{
        this.@im.actor.runtime.js.websocket.WebSocketConnection::jsWebSocket.close();
    }-*/;

    public native int getBufferedAmount() /*-{
        return this.@im.actor.runtime.js.websocket.WebSocketConnection::jsWebSocket.bufferedAmount;
    }-*/;

    public native int getReadyState() /*-{
        return this.@im.actor.runtime.js.websocket.WebSocketConnection::jsWebSocket.readyState;
    }-*/;

    public native String getURL() /*-{
        return this.@im.actor.runtime.js.websocket.WebSocketConnection::jsWebSocket.url;
    }-*/;

    /**
     * Creates the JavaScript WebSocket component and set's all callback handlers.
     *
     * @param url
     */
    private native JavaScriptObject createJSWebSocket(final String url, final WebSocketConnection webSocket) /*-{
        var jsWebSocket = new WebSocket(url);
        jsWebSocket.binaryType = "arraybuffer"

        jsWebSocket.onopen = function () {
            webSocket.@im.actor.runtime.js.websocket.WebSocketConnection::onRawConnected()();
        }

        jsWebSocket.onclose = function () {
            webSocket.@im.actor.runtime.js.websocket.WebSocketConnection::onRawClosed()();
        }

        jsWebSocket.onerror = function () {
            webSocket.@im.actor.runtime.js.websocket.WebSocketConnection::onRawClosed()();
        }

        jsWebSocket.onmessage = function (socketResponse) {
            if (socketResponse.data) {
                webSocket.@im.actor.runtime.js.websocket.WebSocketConnection::onRawMessage(*)(socketResponse.data);
            }
        }

        return jsWebSocket;
    }-*/;
}
