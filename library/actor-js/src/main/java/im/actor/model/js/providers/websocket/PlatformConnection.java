package im.actor.model.js.providers.websocket;

import java.io.IOException;
import java.util.Arrays;
import java.util.Random;

import im.actor.model.crypto.CryptoUtils;
import im.actor.model.droidkit.bser.DataInput;
import im.actor.model.droidkit.bser.DataOutput;
import im.actor.model.log.Log;
import im.actor.model.network.Connection;
import im.actor.model.network.ConnectionCallback;
import im.actor.model.network.ConnectionEndpoint;

/**
 * Created by ex3ndr on 29.04.15.
 */
public class PlatformConnection implements Connection {

    private static final Random RANDOM = new Random();
    private final AsyncConnectionInterface connectionInterface = new ConnectionInterface();

    private final String TAG;
    private final AsyncConnection rawConnection;
    private final ConnectionCallback callback;
    private final PlatformConnectionCreateCallback factoryCallback;
    private final int connectionId;
    private final int mtprotoVersion;
    private final int apiMajorVersion;
    private final int apiMinorVersion;

    private boolean isClosed = false;
    private boolean isOpened = false;
    private boolean isHandshakePerformed = false;
    private byte[] handshakeRandomData;

    public PlatformConnection(int connectionId,
                              int mtprotoVersion,
                              int apiMajorVersion,
                              int apiMinorVersion,
                              ConnectionEndpoint endpoint,
                              ConnectionCallback callback,
                              PlatformConnectionCreateCallback factoryCallback,
                              AsyncConnectionFactory connectionFactory) {
        this.TAG = "Connection#"+connectionId;
        this.connectionId = connectionId;
        this.mtprotoVersion = mtprotoVersion;
        this.apiMajorVersion = apiMajorVersion;
        this.apiMinorVersion = apiMinorVersion;
        this.callback = callback;
        this.factoryCallback = factoryCallback;
        this.rawConnection = connectionFactory.createConnection(endpoint, connectionInterface);
        Log.d(TAG, "Starting connection");
        this.rawConnection.doConnect();
    }

    private synchronized void onConnected() {
        Log.d(TAG, "onConnected");
        if (isClosed) {
            Log.d(TAG, "onConnected:isClosed");
            return;
        }
        if (isOpened) {
            Log.d(TAG, "onConnected:isOpened");
            return;
        }
        isOpened = true;

        Log.d(TAG, "Starting handshake");
        DataOutput handshakeRequest = new DataOutput();
        handshakeRequest.writeByte(mtprotoVersion);
        handshakeRequest.writeByte(apiMajorVersion);
        handshakeRequest.writeByte(apiMinorVersion);
        handshakeRandomData = new byte[32];
        synchronized (RANDOM) {
            RANDOM.nextBytes(handshakeRandomData);
        }
        handshakeRequest.writeInt(handshakeRandomData.length);
        handshakeRequest.writeBytes(handshakeRandomData, 0, handshakeRandomData.length);
        rawConnection.doSend(handshakeRequest.toByteArray());
    }

    private synchronized void onReceived(byte[] data) {
        if (isClosed) {
            return;
        }

        if (!isHandshakePerformed) {
            Log.d(TAG, "Reading handshake response");
            try {
                DataInput handshakeResponse = new DataInput(data);
                int protoVersion = handshakeResponse.readByte();
                int apiMajor = handshakeResponse.readByte();
                int apiMinor = handshakeResponse.readByte();
                byte[] sha256 = handshakeResponse.readBytes(32);
                byte[] localSha256 = CryptoUtils.SHA256(handshakeRandomData);

                if (!Arrays.equals(sha256, localSha256)) {
                    throw new IOException("SHA 256 is incorrect");
                }
                if (protoVersion != 1) {
                    throw new IOException("Incorrect Proto Version, expected: 1, got " + protoVersion + ";");
                }
                if (apiMajor != 1) {
                    throw new IOException("Incorrect Api Major Version, expected: 1, got " + apiMajor + ";");
                }
                if (apiMinor != 0) {
                    throw new IOException("Incorrect Api Minor Version, expected: 0, got " + apiMinor + ";");
                }

                Log.d(TAG, "Handshake successful");
                isHandshakePerformed = true;
                factoryCallback.onConnectionCreated(this);
            } catch (Exception e) {
                e.printStackTrace();
                close();
            }
        } else {
            Log.d(TAG, "Reading full package");
        }
    }

    private synchronized void onClosed() {
        close();
    }

    @Override
    public synchronized void post(byte[] data, int offset, int len) {
        if (isClosed) {
            return;
        }
    }

    @Override
    public synchronized boolean isClosed() {
        return isClosed;
    }

    @Override
    public synchronized void close() {
        if (isClosed) {
            return;
        }
        isClosed = true;

        rawConnection.doClose();

        if (!isOpened || !isHandshakePerformed) {
            factoryCallback.onConnectionCreateError(this);
        } else {
            callback.onConnectionDie();
        }
    }

    private class ConnectionInterface implements AsyncConnectionInterface {

        @Override
        public void onConnected() {
            PlatformConnection.this.onConnected();
        }

        @Override
        public void onReceived(byte[] data) {
            PlatformConnection.this.onReceived(data);
        }

        @Override
        public void onClosed() {
            PlatformConnection.this.onClosed();
        }
    }
}
