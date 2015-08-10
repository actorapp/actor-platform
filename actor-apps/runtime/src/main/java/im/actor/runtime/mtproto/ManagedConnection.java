/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.runtime.mtproto;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Random;

import im.actor.runtime.Crypto;
import im.actor.runtime.Log;
import im.actor.runtime.bser.DataInput;
import im.actor.runtime.bser.DataOutput;
import im.actor.runtime.crypto.CRC32;
import im.actor.runtime.threading.AbsTimerCompat;

public class ManagedConnection implements Connection {

    public static final int CONNECTION_TIMEOUT = 15 * 1000;
    private static final int HANDSHAKE_TIMEOUT = 15 * 1000;
    private static final int RESPONSE_TIMEOUT = 15 * 1000;
    private static final int PING_TIMEOUT = 5 * 60 * 1000;

    private static final int HEADER_PROTO = 0;
    private static final int HEADER_PING = 1;
    private static final int HEADER_PONG = 2;
    private static final int HEADER_DROP = 3;
    private static final int HEADER_REDIRECT = 4;
    private static final int HEADER_ACK = 6;
    private static final int HEADER_HANDSHAKE_REQUEST = 0xFF;
    private static final int HEADER_HANDSHAKE_RESPONSE = 0xFE;

    private static final Random RANDOM = new Random();
    private final AsyncConnectionInterface connectionInterface = new ConnectionInterface();

    private final CRC32 CRC32_ENGINE = new CRC32();
    private final String TAG;
    private final AsyncConnection rawConnection;
    private final ConnectionCallback callback;
    private final ManagedConnectionCreateCallback factoryCallback;
    private final int connectionId;
    private final int mtprotoVersion;
    private final int apiMajorVersion;
    private final int apiMinorVersion;

    private int receivedPackages = 0;
    private int sentPackages = 0;

    private boolean isClosed = false;
    private boolean isOpened = false;
    private boolean isHandshakePerformed = false;
    private byte[] handshakeRandomData;

    private AbsTimerCompat connectionTimeout;
    private AbsTimerCompat handshakeTimeout;
    private AbsTimerCompat pingTask;
    private final HashMap<Long, AbsTimerCompat> schedulledPings = new HashMap<Long, AbsTimerCompat>();
    private final HashMap<Integer, AbsTimerCompat> packageTimers = new HashMap<Integer, AbsTimerCompat>();

    public ManagedConnection(int connectionId,
                             int mtprotoVersion,
                             int apiMajorVersion,
                             int apiMinorVersion,
                             ConnectionEndpoint endpoint,
                             ConnectionCallback callback,
                             ManagedConnectionCreateCallback factoryCallback,
                             AsyncConnectionFactory connectionFactory) {
        this.TAG = "Connection#" + connectionId;
        this.connectionId = connectionId;
        this.mtprotoVersion = mtprotoVersion;
        this.apiMajorVersion = apiMajorVersion;
        this.apiMinorVersion = apiMinorVersion;
        this.callback = callback;
        this.factoryCallback = factoryCallback;
        this.rawConnection = connectionFactory.createConnection(connectionId, endpoint, connectionInterface);
        // Log.d(TAG, "Starting connection");

        handshakeTimeout = im.actor.runtime.Runtime.createTimer(new TimeoutRunnable());
        pingTask = im.actor.runtime.Runtime.createTimer(new PingRunnable());
        connectionTimeout = im.actor.runtime.Runtime.createTimer(new TimeoutRunnable());
        connectionTimeout.schedule(CONNECTION_TIMEOUT);

        this.rawConnection.doConnect();
    }

    // Handshake

    private synchronized void sendHandshakeRequest() {
        // Log.d(TAG, "Starting handshake");

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

        handshakeTimeout.schedule(HANDSHAKE_TIMEOUT);
        rawPost(HEADER_HANDSHAKE_REQUEST, handshakeRequest.toByteArray());
    }

    private synchronized void onHandshakePackage(byte[] data) throws IOException {
        // Log.d(TAG, "Handshake response received");
        DataInput handshakeResponse = new DataInput(data);
        int protoVersion = handshakeResponse.readByte();
        int apiMajor = handshakeResponse.readByte();
        int apiMinor = handshakeResponse.readByte();
        byte[] sha256 = handshakeResponse.readBytes(32);
        byte[] localSha256 = Crypto.SHA256(handshakeRandomData);

        if (!Arrays.equals(sha256, localSha256)) {
            Log.w(TAG, "SHA 256 is incorrect");
//            Log.d(TAG, "Random data: " + CryptoUtils.hex(handshakeRandomData));
//            Log.d(TAG, "Remote SHA256: " + CryptoUtils.hex(sha256));
//            Log.d(TAG, "Local SHA256: " + CryptoUtils.hex(localSha256));
            throw new IOException("SHA 256 is incorrect");
        }
        if (protoVersion != mtprotoVersion) {
            Log.w(TAG, "Incorrect Proto Version, expected: " + mtprotoVersion + ", got " + protoVersion + ";");
            throw new IOException("Incorrect Proto Version, expected: " + mtprotoVersion + ", got " + protoVersion + ";");
        }
        if (apiMajor != apiMajorVersion) {
            Log.w(TAG, "Incorrect Api Major Version, expected: " + apiMajor + ", got " + apiMajor + ";");
            throw new IOException("Incorrect Api Major Version, expected: " + apiMajor + ", got " + apiMajor + ";");
        }
        if (apiMinor != apiMinorVersion) {
            Log.w(TAG, "Incorrect Api Minor Version, expected: " + apiMinor + ", got " + apiMinor + ";");
            throw new IOException("Incorrect Api Minor Version, expected: " + apiMinor + ", got " + apiMinor + ";");
        }

        // Log.d(TAG, "Handshake successful");
        isHandshakePerformed = true;
        factoryCallback.onConnectionCreated(this);
        handshakeTimeout.cancel();
        pingTask.schedule(PING_TIMEOUT);
    }

    // Proto package

    private synchronized void onProtoPackage(byte[] data) throws IOException {
        callback.onMessage(data, 0, data.length);
        refreshTimeouts();
    }

    private synchronized void sendProtoPackage(byte[] data, int offset, int len) throws IOException {
        if (isClosed) {
            return;
        }
        rawPost(HEADER_PROTO, data, offset, len);
    }

    // Ping/Pong

    private synchronized void onPingPackage(byte[] data) throws IOException {
        // Just send pong package to server
        rawPost(HEADER_PONG, data);
        refreshTimeouts();
    }

    private synchronized void onPongPackage(byte[] data) throws IOException {
        DataInput dataInput = new DataInput(data);
        int size = dataInput.readInt();
        if (size != 8) {
            Log.w(TAG, "Received incorrect pong");
            throw new IOException("Incorrect pong payload size");
        }
        long pingId = dataInput.readLong();

        // Log.d(TAG, "Received pong #" + pingId + "...");

        AbsTimerCompat timeoutTask = schedulledPings.remove(pingId);
        if (timeoutTask == null) {
            return;
        }

        timeoutTask.cancel();
        refreshTimeouts();
    }

    private synchronized void sendPingMessage() {
        if (isClosed) {
            return;
        }

        final long pingId = RANDOM.nextLong();
        DataOutput dataOutput = new DataOutput();
        dataOutput.writeInt(8);
        synchronized (RANDOM) {
            dataOutput.writeLong(pingId);
        }

        AbsTimerCompat pingTimeoutTask = im.actor.runtime.Runtime.createTimer(new TimeoutRunnable());
        schedulledPings.put(pingId, pingTimeoutTask);
        pingTimeoutTask.schedule(RESPONSE_TIMEOUT);

        // Log.d(TAG, "Performing ping #" + pingId + "... " + pingTimeoutTask);
        rawPost(HEADER_PING, dataOutput.toByteArray());
    }

    private void refreshTimeouts() {
        // Settings all timeouts to now+RESPONSE_TIMEOUT
        // Simple, but need some logic improvements to support detecting of frame lost.

        for (AbsTimerCompat ping : schedulledPings.values()) {
            ping.schedule(RESPONSE_TIMEOUT);
        }
        for (AbsTimerCompat ackTimeout : packageTimers.values()) {
            ackTimeout.schedule(RESPONSE_TIMEOUT);
        }

        pingTask.schedule(PING_TIMEOUT);
    }

    // Ack

    private synchronized void onAckPackage(byte[] data) throws IOException {
        DataInput ackContent = new DataInput(data);
        int frameId = ackContent.readInt();

        AbsTimerCompat timerCompat = packageTimers.remove(frameId);
        if (timerCompat == null) {
            return;
        }
        timerCompat.cancel();
        refreshTimeouts();
    }

    private synchronized void sendAckPackage(int receivedIndex) throws IOException {
        if (isClosed) {
            return;
        }

        DataOutput ackPackage = new DataOutput();
        ackPackage.writeInt(receivedIndex);
        rawPost(HEADER_ACK, ackPackage.toByteArray());
    }

    // Drop

    private synchronized void onDropPackage(byte[] data) throws IOException {
        DataInput drop = new DataInput(data);
        long messageId = drop.readLong();
        int errorCode = drop.readByte();
        int messageLen = drop.readInt();
        String message = new String(drop.readBytes(messageLen), "UTF-8");
        Log.w(TAG, "Drop received: " + message);
        throw new IOException("Drop received: " + message);
    }

    // Raw callbacks

    private synchronized void onRawConnected() {
        // Log.d(TAG, "onConnected");
        if (isClosed) {
            // Log.d(TAG, "onConnected:isClosed");
            return;
        }
        if (isOpened) {
            // Log.d(TAG, "onConnected:isOpened");
            return;
        }
        isOpened = true;

        connectionTimeout.cancel();

        sendHandshakeRequest();
    }

    private synchronized void onRawReceived(byte[] data) {
        if (isClosed) {
            return;
        }

        // Log.w(TAG, "onRawReceived");

        try {
            DataInput dataInput = new DataInput(data);
            int packageIndex = dataInput.readInt();

            if (receivedPackages != packageIndex) {
                Log.w(TAG, "Invalid package index. Expected: " + receivedPackages + ", got: " + packageIndex);
                throw new IOException("Invalid package index. Expected: " + receivedPackages + ", got: " + packageIndex);
            }
            receivedPackages++;

            int header = dataInput.readByte();
            int dataLength = dataInput.readInt();
            byte[] content = dataInput.readBytes(dataLength);
            int crc32 = dataInput.readInt();

            CRC32_ENGINE.reset();
            CRC32_ENGINE.update(content);

            if (((int) CRC32_ENGINE.getValue()) != crc32) {
                Log.w(TAG, "Incorrect CRC32");
                throw new IOException("Incorrect CRC32");
            }

            // Log.w(TAG, "Received package: " + header);
            if (header == HEADER_HANDSHAKE_RESPONSE) {
                if (isHandshakePerformed) {
                    throw new IOException("Double Handshake");
                }
                onHandshakePackage(content);
            } else {
                if (!isHandshakePerformed) {
                    throw new IOException("Package before Handshake");
                }

                if (header == HEADER_PROTO) {
                    onProtoPackage(content);
                    sendAckPackage(packageIndex);
                } else if (header == HEADER_PING) {
                    onPingPackage(content);
                } else if (header == HEADER_PONG) {
                    onPongPackage(content);
                } else if (header == HEADER_DROP) {
                    onDropPackage(content);
                } else if (header == HEADER_ACK) {
                    onAckPackage(content);
                } else {
                    Log.w(TAG, "Received unknown package #" + header);
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
            close();
        }

        // Log.w(TAG, "onRawReceived:end");
    }

    private synchronized void onRawClosed() {
        // Log.w(TAG, "Received closed event");
        close();
    }

    // Raw send

    private synchronized void rawPost(int header, byte[] data) {
        rawPost(header, data, 0, data.length);
    }

    private synchronized void rawPost(int header, byte[] data, int offset, int len) {
        // Log.w(TAG, "rawPost");
        int packageId = sentPackages++;
        DataOutput dataOutput = new DataOutput();
        dataOutput.writeInt(packageId);
        dataOutput.writeByte(header);
        dataOutput.writeInt(data.length);
        dataOutput.writeBytes(data, offset, len);
        CRC32_ENGINE.reset();
        CRC32_ENGINE.update(data, offset, len);
        dataOutput.writeInt((int) CRC32_ENGINE.getValue());

        if (header == HEADER_PROTO) {
            AbsTimerCompat timeoutTask = im.actor.runtime.Runtime.createTimer(new TimeoutRunnable());
            packageTimers.put(packageId, timeoutTask);
            timeoutTask.schedule(RESPONSE_TIMEOUT);
        }

        rawConnection.doSend(dataOutput.toByteArray());
    }

    // Public methods

    @Override
    public synchronized void post(byte[] data, int offset, int len) {
        // Log.w(TAG, "post");
        if (isClosed) {
            return;
        }
        try {
            sendProtoPackage(data, offset, len);
        } catch (IOException e) {
            e.printStackTrace();
            close();
        }
    }

    @Override
    public synchronized boolean isClosed() {
        return isClosed;
    }

    @Override
    public synchronized void close() {
        // Log.w(TAG, "close");
        if (isClosed) {
            return;
        }
        isClosed = true;

        rawConnection.doClose();

        synchronized (packageTimers) {
            for (Integer id : packageTimers.keySet()) {
                packageTimers.get(id).cancel();
            }
            for (Long ping : schedulledPings.keySet()) {
                schedulledPings.get(ping).cancel();
            }
            schedulledPings.clear();
            packageTimers.clear();
        }

        pingTask.cancel();
        connectionTimeout.cancel();
        handshakeTimeout.cancel();

        if (!isOpened || !isHandshakePerformed) {
            factoryCallback.onConnectionCreateError(this);
        } else {
            callback.onConnectionDie();
        }
    }

    @Override
    public void checkConnection() {
        pingTask.schedule(0);
    }

    // Connection callback

    private class ConnectionInterface implements AsyncConnectionInterface {

        @Override
        public void onConnected() {
            ManagedConnection.this.onRawConnected();
        }

        @Override
        public void onReceived(byte[] data) {
            ManagedConnection.this.onRawReceived(data);
        }

        @Override
        public void onClosed() {
            ManagedConnection.this.onRawClosed();
        }
    }

    // Timer runanbles
    private class PingRunnable implements Runnable {

        @Override
        public void run() {
            sendPingMessage();
        }
    }

    private class TimeoutRunnable implements Runnable {

        @Override
        public void run() {
            // Log.d(TAG, "Timeout " + this);
            close();
        }
    }
}
