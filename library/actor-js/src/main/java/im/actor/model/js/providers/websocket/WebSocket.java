package im.actor.model.js.providers.websocket;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.typedarrays.shared.ArrayBuffer;
import com.google.gwt.typedarrays.shared.TypedArrays;
import com.google.gwt.typedarrays.shared.Uint8Array;
import im.actor.model.droidkit.bser.DataOutput;
import im.actor.model.log.Log;

import java.util.Random;

/**
 * Created by ex3ndr on 07.02.15.
 */
public class WebSocket {

    private static final Random RANDOM = new Random();

    private final JavaScriptObject jsWebSocket;
    private final WebSocketCallback callback;

    public WebSocket(final String url, final WebSocketCallback callback) {
        assert url != null;
        assert IsSupported();

        this.callback = callback;
        this.jsWebSocket = createJSWebSocket(url, this);
    }

    private void onOpen() {
        Log.d("WS", "onOpen");
        callback.onOpen();
    }

    private void onMessage(ArrayBuffer message) {
        Log.d("WS", "onMessage:" + message);
        Uint8Array array = TypedArrays.createUint8Array(message);
        byte[] res = new byte[array.length()];
        for (int i = 0; i < res.length; i++) {
            res[i] = (byte) (array.get(i));
        }
        callback.onMessage(res);
    }

    private void onError() {
        Log.d("WS", "onError");
        callback.onClose();
    }

    private void onClose() {
        Log.d("WS", "onClose");
        callback.onClose();
    }

    // Native interfaces

    public native void send(Uint8Array message) /*-{
        if (message == null)
            return;

        this.@im.actor.model.js.providers.websocket.WebSocket::jsWebSocket.send(message);
    }-*/;

    public native void close() /*-{
        this.@im.actor.model.js.providers.websocket.WebSocket::jsWebSocket.close();
    }-*/;

    public native int getBufferedAmount() /*-{
        return this.@im.actor.model.js.providers.websocket.WebSocket::jsWebSocket.bufferedAmount;
    }-*/;

    public native int getReadyState() /*-{
        return this.@im.actor.model.js.providers.websocket.WebSocket::jsWebSocket.readyState;
    }-*/;

    public native String getURL() /*-{
        return this.@im.actor.model.js.providers.websocket.WebSocket::jsWebSocket.url;
    }-*/;

    /**
     * Creates the JavaScript WebSocket component and set's all callback handlers.
     *
     * @param url
     */
    private native JavaScriptObject createJSWebSocket(final String url, final WebSocket webSocket) /*-{
        var jsWebSocket = new WebSocket(url);
        jsWebSocket.binaryType = "arraybuffer"

        jsWebSocket.onopen = function () {
            webSocket.@im.actor.model.js.providers.websocket.WebSocket::onOpen()();
        }

        jsWebSocket.onclose = function () {
            webSocket.@im.actor.model.js.providers.websocket.WebSocket::onClose()();
        }

        jsWebSocket.onerror = function () {
            webSocket.@im.actor.model.js.providers.websocket.WebSocket::onError()();
        }

        jsWebSocket.onmessage = function (socketResponse) {
            if (socketResponse.data) {
                webSocket.@im.actor.model.js.providers.websocket.WebSocket::onMessage(*)(socketResponse.data);
            }
        }

        return jsWebSocket;
    }-*/;

    /**
     * @return <code>True</code> if the WebSocket component is supported by the current browser
     */
    public static native boolean IsSupported() /*-{
        if ($wnd.WebSocket) {
            return true;
        } else {
            return false;
        }
    }-*/;
}
