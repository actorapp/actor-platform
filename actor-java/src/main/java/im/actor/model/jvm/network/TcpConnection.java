package im.actor.model.jvm.network;

import im.actor.model.log.Log;
import im.actor.model.network.Connection;
import im.actor.model.network.ConnectionCallback;
import im.actor.model.network.ConnectionEndpoint;

import javax.net.ssl.SSLSocketFactory;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.zip.CRC32;

import static im.actor.model.util.StreamingUtils.*;

/**
 * Created by ex3ndr on 06.02.15.
 */
public class TcpConnection implements Connection {

    private static final int CONNECTION_TIMEOUT = 5 * 1000;
    private static final int READ_DIE_TIMEOUT = 15 * 1000; // 5 sec
    private static final int MAX_PACKAGE_SIZE = 1024 * 1024;

    private final String TAG;

    private ConnectionEndpoint endpoint;
    private ConnectionCallback callback;

    private final Socket socket;

    private final ReaderThread readerThread;
    private final WriterThread writerThread;

    private int sentPackets;
    private int receivedPackets;

    private boolean isClosed;

    public TcpConnection(int id, ConnectionEndpoint _endpoint, ConnectionCallback _callback) throws IOException {
        this.TAG = "Connection#" + id;
        this.endpoint = _endpoint;
        this.callback = _callback;

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

//        if (!params.getConfig().isChromeEnabled()) {
        this.socket.setKeepAlive(true);
        this.socket.setTcpNoDelay(true);
//        }

        this.socket.connect(new InetSocketAddress(endpoint.getHost(), endpoint.getPort()), CONNECTION_TIMEOUT);

        // Init socket streams
        this.socket.getInputStream();
        this.socket.getOutputStream();

        // TODO: Implement handshake
        // ???

        readerThread = new ReaderThread();
        writerThread = new WriterThread();
        readerThread.start();
        writerThread.start();
    }

    @Override
    public void post(byte[] data, int offset, int len) {
        writerThread.pushPackage(new Package(data));
    }

    @Override
    public boolean isClosed() {
        return isClosed;
    }

    @Override
    public void close() {
        breakConnection();
    }

    private synchronized void breakConnection() {
        if (!isClosed) {
            Log.w(TAG, "Breaking connection");
            isClosed = true;
            try {
                readerThread.interrupt();
            } catch (final Exception e) {
                Log.e(TAG, e);
            }

            try {
                writerThread.interrupt();
            } catch (final Exception e) {
                Log.e(TAG, e);
            }

//            try {
//                dieThread.interrupt();
//            } catch (final Exception e) {
//                Log.e(TAG, e);
//            }

            try {
                socket.close();
            } catch (IOException e) {
                Log.e(TAG, e);
            }

            callback.onConnectionDie();
        }
    }

    private class ReaderThread extends Thread {
        private ReaderThread() {
            setPriority(Thread.MIN_PRIORITY);
            setName(TAG + "#Reader" + hashCode());
        }

        @Override
        public void run() {
            try {
                while (!isClosed && !isInterrupted()) {
                    if (socket.isClosed()) {
                        Log.d(TAG, "Socket is closed");
                        throw new IOException("Socket is closed");
                    }
                    if (!socket.isConnected()) {
                        Log.d(TAG, "Socket is not connected");
                        throw new IOException("Socket is not connected");
                    }

                    InputStream stream = socket.getInputStream();
                    long start = System.currentTimeMillis();

                    // Length
//                        if (LOG != null && DEBUG) {
//                            LOG.d(TAG, "Reading content length");
//                        }
                    int pkgLen = readInt(stream);
                    if (pkgLen < 0 || pkgLen > MAX_PACKAGE_SIZE) {
                        Log.w(TAG, "Invalid package size: " + pkgLen);
                        throw new IOException("Invalid package size");
                    }

                    // Index
//                        if (LOG != null && DEBUG) {
//                            LOG.d(TAG, "Reading package index");
//                        }
                    int pkgIndex = readInt(stream);
                    int expectedIndex = receivedPackets++;
                    if (pkgIndex != expectedIndex) {
//                            if (LOG != null) {
//                                LOG.w(TAG, "Wrong seq. Expected " + expectedIndex + ", got " + pkgIndex);
//                            }
                        Log.w(TAG, "Wrong seq. Expected " + expectedIndex + ", got " + pkgIndex);
                        throw new IOException("Wrong number of received packets");
                    }

                    // Content
//                        if (LOG != null && DEBUG) {
//                            LOG.d(TAG, "Reading package content of " + pkgLen + " bytes");
//                        }
                    byte[] pkg = readBytes(pkgLen - 8, stream);

                    // CRC32
//                        if (LOG != null && DEBUG) {
//                            LOG.d(TAG, "Reading CRC32");
//                        }
                    int pkgCrc = readInt(stream);
                    CRC32 crc32 = new CRC32();
                    crc32.update(intToBytes(pkgLen));
                    crc32.update(intToBytes(pkgIndex));
                    crc32.update(pkg);
                    int localCrc = (int) crc32.getValue();

                    if (localCrc != pkgCrc) {
                        Log.w(TAG, "Package crc32 expected: " + localCrc + ", got: " + pkgCrc);
                        throw new IOException("Wrong CRC");
                    }

//                        if (LOG != null && DEBUG) {
//                            LOG.d(TAG, "Read #" + pkgIndex + " in " + (System.currentTimeMillis() - start) + " ms");
//                        }
                    callback.onMessage(pkg, 0, pkgLen);
                }
            } catch (final Throwable e) {
                Log.e(TAG, e);
                breakConnection();
            }
        }
    }

    private class WriterThread extends Thread {

        private final CRC32 crc32 = new CRC32();
        private final ConcurrentLinkedQueue<Package> packages = new ConcurrentLinkedQueue<Package>();

        public WriterThread() {
            setPriority(Thread.MIN_PRIORITY);
            setName(TAG + "#Writer" + hashCode());
        }

        public void pushPackage(final Package p) {
            packages.add(p);
            synchronized (packages) {
                packages.notifyAll();
            }
        }

        @Override
        public void run() {
            while (!isClosed && !isInterrupted()) {
                Package p;
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

                try {
                    byte[] data = p.data;
                    int length = data.length + 8;

                    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                    writeInt(length, outputStream);
                    writeInt(sentPackets, outputStream);
                    writeBytes(data, outputStream);
                    crc32.reset();
                    crc32.update(outputStream.toByteArray());
                    writeInt((int) crc32.getValue(), outputStream);
                    socket.getOutputStream().write(outputStream.toByteArray());
                    socket.getOutputStream().flush();
                    // onWrite();

                    sentPackets++;
                } catch (final Exception e) {
                    Log.e(TAG, e);
                    breakConnection();
                }
            }
        }
    }

    private class Package {
        public byte[] data;

        public Package() {

        }

        private Package(final byte[] data) {
            this.data = data;
        }
    }
}
