package im.actor.model.jvm;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentLinkedQueue;

import javax.net.ssl.SSLSocketFactory;

import im.actor.model.crypto.CryptoUtils;
import im.actor.model.droidkit.bser.DataInput;
import im.actor.model.droidkit.bser.DataOutput;
import im.actor.model.log.Log;
import im.actor.model.network.Connection;
import im.actor.model.network.ConnectionCallback;
import im.actor.model.network.ConnectionEndpoint;
import im.actor.model.util.CRC32;

/**
 * Created by ex3ndr on 13.04.15.
 */
public class JavaTcpConnection implements Connection {

    private static final int CONNECTION_TIMEOUT = 5 * 1000;
    private static final int HANDSHAKE_TIMEOUT = 5 * 1000;
    private static final int RESPONSE_TIMEOUT = 5 * 1000;
    private static final int PING_TIMEOUT = 5 * 60 * 1000;

    private static final int HEADER_PROTO = 0;
    private static final int HEADER_PING = 1;
    private static final int HEADER_PONG = 2;
    private static final int HEADER_DROP = 3;
    private static final int HEADER_REDIRECT = 4;
    private static final int HEADER_ACK = 6;

    private static final Random RANDOM = new Random();
    private static final Timer DIE_TIMER = new Timer();

    private final String TAG;
    private final ConnectionCallback callback;
    private final Socket socket;
    private final InputStream inputStream;
    private final OutputStream outputStream;
    private final ReaderThread readerThread;
    private final WriterThread writerThread;
    private TimerTask pingTask;
    private final HashMap<Long, TimerTask> schedulledPings = new HashMap<Long, TimerTask>();
    private final HashMap<Integer, TimerTask> packageTimers = new HashMap<Integer, TimerTask>();

    // Connection state
    private int sentPackets;
    private int receivedPackets;
    private boolean isClosed;

    public JavaTcpConnection(int id,
                             int mtprotoVersion,
                             int apiMajorVersion,
                             int apiMinorVersion,
                             ConnectionEndpoint endpoint,
                             ConnectionCallback callback) throws IOException {
        this.TAG = "Connection#" + id;
        this.callback = callback;
        this.isClosed = false;
        this.sentPackets = 0;
        this.receivedPackets = 0;

        // Log.d(TAG, "Creating socket...");
        switch (endpoint.getType()) {
            case TCP:
                socket = new Socket();
                break;
            case TCP_TLS:
                socket = SSLSocketFactory.getDefault().createSocket();
                break;
            default:
                throw new IOException("Unsupported endpoint type: " + endpoint.getType());
        }

        this.socket.setKeepAlive(false);
        this.socket.setTcpNoDelay(true);

        // Log.d(TAG, "Connecting socket...");

        this.socket.connect(new InetSocketAddress(endpoint.getHost(), endpoint.getPort()), CONNECTION_TIMEOUT);

        // Log.d(TAG, "Performing handshake...");

        // Init socket streams
        inputStream = this.socket.getInputStream();
        outputStream = this.socket.getOutputStream();

        // Handshake request
        DataOutput handshakeRequest = new DataOutput();
        handshakeRequest.writeByte(mtprotoVersion);
        handshakeRequest.writeByte(apiMajorVersion);
        handshakeRequest.writeByte(apiMinorVersion);
        byte[] randomData = new byte[32];
        synchronized (RANDOM) {
            RANDOM.nextBytes(randomData);
        }
        handshakeRequest.writeInt(randomData.length);
        handshakeRequest.writeBytes(randomData, 0, randomData.length);
        outputStream.write(handshakeRequest.toByteArray());
        outputStream.flush();

        // Log.d(TAG, "Reading handshake response...");
        // Handshake response
        socket.setSoTimeout(HANDSHAKE_TIMEOUT);
        byte[] data = readBytes(3 + 32);
        socket.setSoTimeout(0);
        DataInput handshakeResponse = new DataInput(data);
        int protoVersion = handshakeResponse.readByte();
        int apiMajor = handshakeResponse.readByte();
        int apiMinor = handshakeResponse.readByte();
        byte[] sha256 = handshakeResponse.readBytes(32);
        byte[] localSha256 = CryptoUtils.SHA256(randomData);

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

        // Log.d(TAG, "Handshake completed.");

        readerThread = new ReaderThread();
        writerThread = new WriterThread();
        readerThread.start();
        writerThread.start();

        pingTask = new PingTask();
        DIE_TIMER.schedule(pingTask, PING_TIMEOUT);
    }

    @Override
    public synchronized void post(byte[] data, int offset, int len) {
        post(HEADER_PROTO, data, offset, len);
    }

    protected synchronized void post(int header, byte[] data) {
        post(header, data, 0, data.length);
    }

    protected synchronized void post(int header, byte[] data, int offset, int len) {
        writerThread.pushPackage(new WritePackage(header, data, offset, len));
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

        try {
            readerThread.interrupt();
        } catch (Exception e) {
            Log.e(TAG, e);
        }

        try {
            writerThread.interrupt();
        } catch (Exception e) {
            Log.e(TAG, e);
        }

        try {
            socket.close();
        } catch (IOException e) {
            Log.e(TAG, e);
        }

        callback.onConnectionDie();

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
    }

    private void onServerAck(int packageId) {
        synchronized (packageTimers) {
            TimerTask task = packageTimers.remove(packageId);
            if (task == null) {
                return;
            }
            task.cancel();

            refreshTimeouts();
        }
    }

    private void onServerPong(long pingId) {
        synchronized (packageTimers) {
            TimerTask task = schedulledPings.remove(pingId);
            if (task == null) {
                return;
            }
            task.cancel();

            refreshTimeouts();
        }
    }

    private void refreshTimeouts() {
        for (Long ping : schedulledPings.keySet().toArray(new Long[0])) {
            // Remove old
            TimerTask oldTask = schedulledPings.remove(ping);
            oldTask.cancel();

            // Add new
            PingTimeoutTask newTask = new PingTimeoutTask(ping);
            schedulledPings.put(ping, newTask);
            DIE_TIMER.schedule(newTask, RESPONSE_TIMEOUT);
        }
        for (Integer id : packageTimers.keySet().toArray(new Integer[0])) {
            // Remove old
            TimerTask oldTask = packageTimers.get(id);
            oldTask.cancel();

            // Add new
            FrameTimeoutTask newTask = new FrameTimeoutTask(id);
            packageTimers.put(id, newTask);
            DIE_TIMER.schedule(newTask, RESPONSE_TIMEOUT);
        }

        pingTask.cancel();

        pingTask = new PingTask();
        DIE_TIMER.schedule(pingTask, PING_TIMEOUT);
    }

    private class ReaderThread extends Thread {

        private final CRC32 crc32Engine = new CRC32();

        private ReaderThread() {
            setName(TAG + "#Reader" + hashCode());
        }

        @Override
        public void run() {
            try {
                while (!isClosed()) {
                    if (socket.isClosed()) {
                        throw new IOException("Socket is closed");
                    }
                    if (!socket.isConnected()) {
                        throw new IOException("Socket is not connected");
                    }

                    // Reading package headers
                    // Log.d(TAG, "Waiting for frame header...");
                    byte[] packageHeader = readBytes(9);
                    DataInput dataInput = new DataInput(packageHeader);
                    int receivedPackageIndex = dataInput.readInt();
                    if (receivedPackageIndex != receivedPackets) {
                        throw new IOException("Received frame with incorrect index. " +
                                "Expected: " + receivedPackets + ", got: " + receivedPackageIndex);
                    }
                    receivedPackets++;
                    int header = dataInput.readByte();
                    int size = dataInput.readInt();

                    // Reading package body
                    // Log.d(TAG, "Reading frame body for #" + receivedPackageIndex);
                    byte[] body = readBytes(size + 4);
                    dataInput = new DataInput(body);
                    byte[] contents = dataInput.readBytes(size);

                    // Checking CRC32
                    long crc32 = dataInput.readUInt();
                    crc32Engine.reset();
                    crc32Engine.update(contents);
                    long localCrc32 = crc32Engine.getValue();
                    if (localCrc32 != crc32) {
                        throw new IOException("Received frame contents with incorrect crc32");
                    }

                    // Processing package
                    if (header == HEADER_PROTO) {
                        // Log.d(TAG, "Received proto frame");
                        callback.onMessage(contents, 0, contents.length);

                        DataOutput ackPackage = new DataOutput();
                        ackPackage.writeInt(receivedPackageIndex);
                        post(HEADER_ACK, ackPackage.toByteArray());
                    } else if (header == HEADER_PING) {
                        // Log.d(TAG, "Received ping frame");
                        post(HEADER_PONG, contents);
                    } else if (header == HEADER_PONG) {
                        // Log.d(TAG, "Received pong frame");
                        DataInput pongInput = new DataInput(contents);
                        int pongLen = pongInput.readInt();
                        if (pongLen != 8) {
                            // Log.w(TAG, "Pong invalid content length, got: " + pongLen);
                            continue;
                        }
                        onServerPong(pongInput.readLong());
                    } else if (header == HEADER_ACK) {
                        // Log.d(TAG, "Received ack frame");
                        DataInput ackContent = new DataInput(contents);
                        int frameId = ackContent.readInt();
                        onServerAck(frameId);
                    } else if (header == HEADER_REDIRECT) {
                        DataInput redirectContent = new DataInput(contents);
                        int hostLen = redirectContent.readInt();
                        String host = new String(redirectContent.readBytes(hostLen), "UTF-8");
                        int port = redirectContent.readInt();
                        int timeout = redirectContent.readInt();
                        // TODO: Implement redirect
                        throw new IOException("Received redirect frame: " + host + ":" + port + " with timeout " + timeout + " sec");
                    } else if (header == HEADER_DROP) {
                        DataInput drop = new DataInput(contents);
                        long messageId = drop.readLong();
                        int errorCode = drop.readByte();
                        int messageLen = drop.readInt();
                        String message = new String(drop.readBytes(messageLen), "UTF-8");
                        // Log.d(TAG, "Received drop frame: " + message);

                        throw new IOException("Received drop frame: " + message);
                    } else {
                        Log.w(TAG, "Received unknown frame #" + header);
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
                close();
            }
        }
    }

    /**
     * Package send thread
     */
    private class WriterThread extends Thread {

        private final CRC32 crc32Engine = new CRC32();

        private final ConcurrentLinkedQueue<WritePackage> packages = new ConcurrentLinkedQueue<WritePackage>();

        public WriterThread() {
            setName(TAG + "#Writer" + hashCode());
        }

        /**
         * Send package to connection
         *
         * @param p package
         */
        public void pushPackage(final WritePackage p) {
            packages.add(p);
            synchronized (packages) {
                packages.notifyAll();
            }
        }

        @Override
        public void run() {
            try {
                while (!isClosed()) {

                    // Pooling of package from queue
                    WritePackage p;
                    synchronized (packages) {
                        p = packages.poll();
                        if (p == null) {
                            try {
                                packages.wait();
                            } catch (final InterruptedException e) {
                                return;
                            }
                            p = packages.poll();
                        }
                    }
                    if (p == null) {
                        continue;
                    }

                    // Start package send
                    // Log.d(TAG, "Sending frame #" + sentPackets);

                    // Prepare package
                    final int packageId = sentPackets++;
                    DataOutput dataOutput = new DataOutput();
                    dataOutput.writeInt(packageId);
                    dataOutput.writeByte(p.getHeader());
                    dataOutput.writeInt(p.getContent().length);
                    dataOutput.writeBytes(p.getContent(), p.getOffset(), p.getLen());
                    crc32Engine.reset();
                    crc32Engine.update(p.getContent(), p.getOffset(), p.getLen());
                    dataOutput.writeInt((int) crc32Engine.getValue());
                    byte[] destPackage = dataOutput.toByteArray();

                    // Setting Ack timeout
                    if (p.getHeader() == HEADER_PROTO) {
                        synchronized (packageTimers) {
                            TimerTask timeoutTask = new TimerTask() {
                                @Override
                                public void run() {
                                    // Log.d(TAG, "Response #" + packageId + " not received in time");
                                    close();
                                }
                            };
                            packageTimers.put(packageId, timeoutTask);
                            DIE_TIMER.schedule(timeoutTask, RESPONSE_TIMEOUT);
                        }
                    }

                    // Writing package to socket
                    outputStream.write(destPackage);
                    outputStream.flush();
                }
            } catch (IOException e) {
                e.printStackTrace();
                close();
            }
        }
    }

    private class WritePackage {
        private int header;
        private byte[] content;
        private int offset;
        private int len;

        private WritePackage(int header, byte[] content, int offset, int len) {
            this.header = header;
            this.content = content;
            this.offset = offset;
            this.len = len;
        }

        public int getHeader() {
            return header;
        }

        public byte[] getContent() {
            return content;
        }

        public int getOffset() {
            return offset;
        }

        public int getLen() {
            return len;
        }
    }

    class PingTimeoutTask extends TimerTask {
        private long pingId;

        public PingTimeoutTask(long pingId) {
            this.pingId = pingId;
        }

        @Override
        public void run() {
            // Log.d(TAG, "Ping #" + pingId + " is timed out");
            close();
        }
    }

    class FrameTimeoutTask extends TimerTask {
        private int frameId;

        FrameTimeoutTask(int frameId) {
            this.frameId = frameId;
        }

        @Override
        public void run() {
            // Log.d(TAG, "Response #" + frameId + " not received in time");
            close();
        }
    }

    class PingTask extends TimerTask {
        @Override
        public void run() {
            if (isClosed()) {
                return;
            }
            final long pingId = RANDOM.nextLong();
            DataOutput dataOutput = new DataOutput();
            dataOutput.writeInt(8);
            synchronized (RANDOM) {
                dataOutput.writeLong(pingId);
            }

            PingTimeoutTask pingTimeout = new PingTimeoutTask(pingId);
            synchronized (packageTimers) {
                schedulledPings.put(pingId, pingTimeout);
            }
            DIE_TIMER.schedule(pingTimeout, RESPONSE_TIMEOUT);

            // Log.d(TAG, "Performing ping #" + pingId + "...");
            post(HEADER_PING, dataOutput.toByteArray());
        }
    }

    private byte[] readBytes(int count) throws IOException {
        byte[] res = new byte[count];
        int offset = 0;
        while (offset < res.length) {
            int readed = inputStream.read(res, offset, res.length - offset);
            if (readed > 0) {
                offset += readed;
            } else if (readed < 0) {
                throw new IOException();
            } else {
                Thread.yield();
            }
        }
        return res;
    }
}
