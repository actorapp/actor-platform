package im.actor.model.js.providers.websocket;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.typedarrays.shared.ArrayBuffer;
import com.google.gwt.typedarrays.shared.TypedArrays;
import com.google.gwt.typedarrays.shared.Uint8Array;
import im.actor.model.log.Log;
import im.actor.model.network.ConnectionEndpoint;

/**
 * Created by ex3ndr on 29.04.15.
 */
public class WebSocketAsyncConnection extends AsyncConnection {

    private JavaScriptObject jsWebSocket;

    public WebSocketAsyncConnection(ConnectionEndpoint endpoint, AsyncConnectionInterface connection) {
        super(endpoint, connection);
    }

    @Override
    public void doConnect() {
        String url;
        if (getEndpoint().getType() == ConnectionEndpoint.Type.WS) {
            url = "ws://" + getEndpoint().getHost() + ":" + getEndpoint().getPort() + "/";
        } else if (getEndpoint().getType() == ConnectionEndpoint.Type.WS_TLS) {
            url = "wss://" + getEndpoint().getHost() + ":" + getEndpoint().getPort() + "/";
        } else {
            throw new RuntimeException();
        }
        Log.d("WS", "Connecting to " + url);
        this.jsWebSocket = createJSWebSocket(url, this);
    }

    @Override
    public void doSend(byte[] data) {
        Uint8Array push = TypedArrays.createUint8Array(data.length);
        for (int i = 0; i < data.length; i++) {
            push.set(i, data[i]);
        }
        send(push);
    }

    @Override
    public void doClose() {
        close();
    }

    private void onRawMessage(ArrayBuffer message) {
        Uint8Array array = TypedArrays.createUint8Array(message);
        byte[] res = new byte[array.length()];
        for (int i = 0; i < res.length; i++) {
            res[i] = (byte) (array.get(i));
        }
        onReceived(res);
    }

    // Native interfaces

    public native void send(Uint8Array message) /*-{
        if (message == null)
            return;

        this.@im.actor.model.js.providers.websocket.WebSocketAsyncConnection::jsWebSocket.send(message);
    }-*/;

    public native void close() /*-{
        this.@im.actor.model.js.providers.websocket.WebSocketAsyncConnection::jsWebSocket.close();
    }-*/;

    public native int getBufferedAmount() /*-{
        return this.@im.actor.model.js.providers.websocket.WebSocketAsyncConnection::jsWebSocket.bufferedAmount;
    }-*/;

    public native int getReadyState() /*-{
        return this.@im.actor.model.js.providers.websocket.WebSocketAsyncConnection::jsWebSocket.readyState;
    }-*/;

    public native String getURL() /*-{
        return this.@im.actor.model.js.providers.websocket.WebSocketAsyncConnection::jsWebSocket.url;
    }-*/;

    /**
     * Creates the JavaScript WebSocket component and set's all callback handlers.
     *
     * @param url
     */
    private native JavaScriptObject createJSWebSocket(final String url, final WebSocketAsyncConnection webSocket) /*-{
        var jsWebSocket = new WebSocket(url);
        jsWebSocket.binaryType = "arraybuffer"

        jsWebSocket.onopen = function () {
            webSocket.@im.actor.model.js.providers.websocket.WebSocketAsyncConnection::onConnected()();
        }

        jsWebSocket.onclose = function () {
            webSocket.@im.actor.model.js.providers.websocket.WebSocketAsyncConnection::onClosed()();
        }

        jsWebSocket.onerror = function () {
            webSocket.@im.actor.model.js.providers.websocket.WebSocketAsyncConnection::onClosed()();
        }

        jsWebSocket.onmessage = function (socketResponse) {
            if (socketResponse.data) {
                webSocket.@im.actor.model.js.providers.websocket.WebSocketAsyncConnection::onRawMessage(*)(socketResponse.data);
            }
        }

        return jsWebSocket;
    }-*/;
}
