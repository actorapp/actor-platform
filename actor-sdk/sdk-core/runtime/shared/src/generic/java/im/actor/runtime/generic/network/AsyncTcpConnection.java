/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.runtime.generic.network;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.net.ssl.SSLSocketFactory;

import im.actor.runtime.Log;
import im.actor.runtime.bser.DataInput;
import im.actor.runtime.bser.DataOutput;
import im.actor.runtime.mtproto.AsyncConnection;
import im.actor.runtime.mtproto.AsyncConnectionInterface;
import im.actor.runtime.mtproto.ConnectionEndpoint;
import im.actor.runtime.mtproto.ManagedConnection;

public class AsyncTcpConnection extends AsyncConnection {

    private final ExecutorService connectExecutor = Executors.newSingleThreadExecutor();

    private final String TAG;

    private Socket socket;
    private InputStream inputStream;
    private OutputStream outputStream;
    private WriterThread writerThread;
    private ReaderThread readerThread;

    private boolean isConnected = false;
    private boolean isClosed = false;

    public AsyncTcpConnection(int id, ConnectionEndpoint endpoint, AsyncConnectionInterface connection) {
        super(endpoint, connection);

        this.TAG = "ConnectionTcp#" + id;
    }

    @Override
    public void doConnect() {
        connectExecutor.submit(new Runnable() {
            @Override
            public void run() {
                try {
                    ConnectionEndpoint endpoint = getEndpoint();

                    Socket socket = new Socket();

                    // Configure socket
                    socket.setKeepAlive(false);
                    socket.setTcpNoDelay(true);

                    // Connecting
                    socket.connect(new InetSocketAddress(endpoint.getHost(), endpoint.getPort()), ManagedConnection.CONNECTION_TIMEOUT);

                    // Converting SSL socket
                    if (endpoint.getType() == ConnectionEndpoint.Type.TCP_TLS) {
                        SSLSocketFactory socketFactory = (SSLSocketFactory) SSLSocketFactory.getDefault();
                        socket = socketFactory.createSocket(socket,
                                endpoint.getHost(), endpoint.getPort(), true);
                    }

                    // Init streams
                    socket.getInputStream();
                    socket.getOutputStream();

                    onSocketCreated(socket);
                } catch (Throwable e) {
                    e.printStackTrace();
                    crashConnection();
                }
            }
        });
    }

    @Override
    public void doSend(byte[] data) {
        writerThread.pushPackage(data);
    }

    @Override
    public void doClose() {
        crashConnection();
    }


    private synchronized void onSocketCreated(Socket socket) throws IOException {
        if (isClosed) {
            Log.w(TAG, "Socket created after external close: disposing");
            throw new IOException("Socket created after external close: disposing");
        }

        this.socket = socket;
        this.inputStream = socket.getInputStream();
        this.outputStream = socket.getOutputStream();

        this.isClosed = false;
        this.isConnected = true;

        this.readerThread = new ReaderThread();
        this.readerThread.start();

        this.writerThread = new WriterThread();
        this.writerThread.start();

        onConnected();
    }

    private synchronized void onRawReceived(byte[] data) throws IOException {
        if (!isConnected) {
            Log.d(TAG, "onRawReceived: Not connected");
            return;
        }

        onReceived(data);
    }

    private synchronized void crashConnection() {
        Log.d(TAG, "Crashing Connection");

        if (isClosed) {
            return;
        }
        isClosed = true;
        isConnected = false;

        if (writerThread != null) {
            writerThread.interrupt();
        }
        if (readerThread != null) {
            readerThread.interrupt();
        }
        writerThread = null;
        readerThread = null;

        if (socket != null) {
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if (inputStream != null) {
            try {
                inputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if (outputStream != null) {
            try {
                outputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        socket = null;
        inputStream = null;
        outputStream = null;

        onClosed();
    }


    private class WriterThread extends Thread {
        private final ConcurrentLinkedQueue<byte[]> packages = new ConcurrentLinkedQueue<byte[]>();

        public WriterThread() {
            setName(TAG + "#Writer");
        }

        /**
         * Send package to connection
         *
         * @param p package
         */
        public void pushPackage(final byte[] p) {
            packages.add(p);
            synchronized (packages) {
                packages.notifyAll();
            }
        }

        @Override
        public void run() {
            try {
                while (isConnected) {

                    // Pooling of package from queue
                    byte[] p;
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

                    outputStream.write(p);
                    outputStream.flush();
                }
            } catch (IOException e) {
                e.printStackTrace();
                crashConnection();
            }
        }
    }

    private class ReaderThread extends Thread {

        private ReaderThread() {
            setName(TAG + "#Reader");
        }

        @Override
        public void run() {
            try {
                while (isConnected) {
                    // Reading package headers
                    byte[] header = readBytes(9);
                    DataInput dataInput = new DataInput(header);
                    int receivedPackageIndex = dataInput.readInt();
                    int headerValue = dataInput.readByte();
                    int size = dataInput.readInt();

                    if (size > 1024 * 1024) {
                        throw new IOException("Incorrect size");
                    }

                    // Reading package body
                    byte[] body = readBytes(size + 4);

                    DataOutput dataOutput = new DataOutput();
                    dataOutput.writeBytes(header);
                    dataOutput.writeBytes(body);
                    onRawReceived(dataOutput.toByteArray());
                }
            } catch (IOException e) {
                e.printStackTrace();
                crashConnection();
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
}
