/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.core.network.mtp.actors;

import java.io.IOException;

import im.actor.core.network.ActorApi;
import im.actor.core.network.Endpoints;
import im.actor.core.network.NetworkState;
import im.actor.core.network.mtp.MTProto;
import im.actor.core.network.mtp.entity.ProtoMessage;
import im.actor.core.util.ExponentialBackoff;
import im.actor.runtime.*;
import im.actor.runtime.actors.Actor;
import im.actor.runtime.actors.ActorCreator;
import im.actor.runtime.actors.ActorRef;
import im.actor.runtime.actors.ActorSelection;
import im.actor.runtime.actors.ActorSystem;
import im.actor.runtime.actors.Props;
import im.actor.runtime.bser.DataInput;
import im.actor.runtime.bser.DataOutput;
import im.actor.runtime.mtproto.Connection;
import im.actor.runtime.mtproto.ConnectionCallback;
import im.actor.runtime.mtproto.CreateConnectionCallback;
import im.actor.runtime.threading.AtomicIntegerCompat;

/**
 * Possible problems
 * * Creating connections after actor kill
 */
public class ManagerActor extends Actor {

    private static final String TAG = "Manager";

    public static ActorRef manager(final MTProto mtProto) {
        return ActorSystem.system().actorOf(
                new ActorSelection(Props.create(ManagerActor.class, new ActorCreator<ManagerActor>() {
                    @Override
                    public ManagerActor create() {
                        return new ManagerActor(mtProto);
                    }
                }).changeDispatcher("network"), mtProto.getActorPath() + "/manager"));
    }

    private static final AtomicIntegerCompat NEXT_CONNECTION = im.actor.runtime.Runtime.createAtomicInt(1);

    private final MTProto mtProto;
    private final Endpoints endpoints;
    private final long authId;
    private final long sessionId;
    private final boolean isEnableLog;

    // Connection
    private int currentConnectionId;
    private Connection currentConnection;
    private NetworkState networkState = NetworkState.UNKNOWN;

    // Creating
    private boolean isCheckingConnections = false;
    private final ExponentialBackoff backoff;

    private ActorRef receiver;
    private ActorRef sender;

    public ManagerActor(MTProto mtProto) {
        this.mtProto = mtProto;
        this.endpoints = mtProto.getEndpoints();
        this.authId = mtProto.getAuthId();
        this.sessionId = mtProto.getSessionId();
        this.isEnableLog = mtProto.isEnableLog();
        backoff = new ExponentialBackoff(mtProto.getMinDelay(), mtProto.getMaxDelay(), mtProto.getMaxFailureCount());
    }

    @Override
    public void preStart() {
        receiver = ReceiverActor.receiver(mtProto);
        sender = PusherActor.senderActor(mtProto);
        connectionStateChanged();
        checkConnection();
    }

    @Override
    public void postStop() {
        this.receiver = null;
        this.sender = null;
        currentConnectionId = -1;
        if (currentConnection != null) {
            currentConnection.close();
            currentConnection = null;
        }
        connectionStateChanged();
    }

    @Override
    public void onReceive(Object message) {

        // Connections
        if (message instanceof ConnectionCreated) {
            ConnectionCreated c = (ConnectionCreated) message;
            onConnectionCreated(c.connectionId, c.connection);
        } else if (message instanceof ConnectionCreateFailure) {
            onConnectionCreateFailure();
        } else if (message instanceof ConnectionDie) {
            onConnectionDie(((ConnectionDie) message).connectionId);
        } else if (message instanceof PerformConnectionCheck) {
            checkConnection();
        } else if (message instanceof NetworkChanged) {
            onNetworkChanged(((NetworkChanged) message).state);
        } else if (message instanceof ForceNetworkCheck) {
            forceNetworkCheck();
        }
        // Messages
        else if (message instanceof OutMessage) {
            OutMessage m = (OutMessage) message;
            onOutMessage(m.message, m.offset, m.len);
        } else if (message instanceof InMessage) {
            InMessage m = (InMessage) message;
            onInMessage(m.data, m.offset, m.len);
        } else {
            drop(message);
        }
    }

    private void onConnectionCreated(int id, Connection connection) {

        if (connection.isClosed()) {
            if (isEnableLog) {
                Log.w(TAG, "Unable to register connection #" + id + ": already closed");
            }
            return;
        }

        if (currentConnectionId == id) {
            if (isEnableLog) {
                Log.w(TAG, "Unable to register connection #" + id + ": already have connection");
            }
            return;
        }

        Log.d(TAG, "Connection #" + id + " created");

        if (currentConnection != null) {
            currentConnection.close();
            currentConnectionId = 0;
        }

        currentConnectionId = id;
        currentConnection = connection;
        connectionStateChanged();


        backoff.onSuccess();
        isCheckingConnections = false;
        requestCheckConnection();

        sender.send(new PusherActor.ConnectionCreated());
    }

    private void onConnectionCreateFailure() {
        Log.w(TAG, "Connection create failure");

        backoff.onFailure();
        isCheckingConnections = false;
        requestCheckConnection(backoff.exponentialWait());
    }

    private void onConnectionDie(int id) {
        Log.w(TAG, "Connection #" + id + " dies");

        if (currentConnectionId == id) {
            currentConnectionId = 0;
            currentConnection = null;
            connectionStateChanged();
            requestCheckConnection();
        }
    }

    private void onNetworkChanged(NetworkState state) {
        Log.w(TAG, "Network configuration changed: " + state);
        this.networkState = state;
        backoff.reset();
        checkConnection();
    }

    private void forceNetworkCheck() {
        if (currentConnection != null) {
            currentConnection.checkConnection();
        }
    }

    private void requestCheckConnection() {
        requestCheckConnection(0);
    }

    private void requestCheckConnection(long wait) {
        if (!isCheckingConnections) {
            if (currentConnection == null) {
                if (wait == 0) {
                    Log.w(TAG, "Requesting connection creating");
                } else {
                    Log.w(TAG, "Requesting connection creating in " + wait + " ms");
                }
            }
            self().sendOnce(new PerformConnectionCheck(), wait);
        }
    }

    private void checkConnection() {
        if (isCheckingConnections) {
            return;
        }

        if (currentConnection == null) {
            if (networkState == NetworkState.NO_CONNECTION) {
                Log.d(TAG, "Not trying to create connection: Not network available");
                return;
            }
            Log.d(TAG, "Trying to create connection...");

            isCheckingConnections = true;

            final int id = NEXT_CONNECTION.getAndIncrement();

            Network.createConnection(id, ActorApi.MTPROTO_VERSION,
                    ActorApi.API_MAJOR_VERSION,
                    ActorApi.API_MINOR_VERSION,
                    endpoints.fetchEndpoint(), new ConnectionCallback() {

                        @Override
                        public void onConnectionRedirect(String host, int port, int timeout) {
                            // TODO: Implement better processing
                            self().send(new ConnectionDie(id));
                        }

                        @Override
                        public void onMessage(byte[] data, int offset, int len) {
                            self().send(new InMessage(data, offset, len));
                        }

                        @Override
                        public void onConnectionDie() {
                            self().send(new ConnectionDie(id));
                        }
                    }, new CreateConnectionCallback() {
                        @Override
                        public void onConnectionCreated(Connection connection) {
                            self().send(new ConnectionCreated(id, connection));
                        }

                        @Override
                        public void onConnectionCreateError() {
                            self().send(new ConnectionCreateFailure());
                        }
                    });
        }
    }

    private void connectionStateChanged() {
        mtProto.getCallback().onConnectionsCountChanged(currentConnection != null ? 1 : 0);
    }

    private void onInMessage(byte[] data, int offset, int len) {
        // Log.d(TAG, "Received package");

        DataInput bis = new DataInput(data, offset, len);
        try {
            long authId = bis.readLong();
            long sessionId = bis.readLong();

            if (authId != this.authId || sessionId != this.sessionId) {
                throw new IOException("Incorrect header");
            }

            long messageId = bis.readLong();
            byte[] payload = bis.readProtoBytes();

            receiver.send(new ProtoMessage(messageId, payload));
        } catch (IOException e) {
            Log.w(TAG, "Closing connection: incorrect package");
            e.printStackTrace();

            if (currentConnection != null) {
                currentConnection.close();
                currentConnection = null;
                currentConnectionId = 0;
                // Log.d(TAG, "Set connection #" + 0);
            }
            checkConnection();
        }
    }

    private void onOutMessage(byte[] data, int offset, int len) {
        // Log.d(TAG, "Out message");

        // Cleanup bad connection
        if (currentConnection != null && currentConnection.isClosed()) {
            currentConnection = null;
            // Log.d(TAG, "Set connection #" + 0);
            currentConnectionId = 0;
            checkConnection();
        }

        if (currentConnection != null) {
            DataOutput bos = new DataOutput();
            bos.writeLong(authId);
            bos.writeLong(sessionId);
            bos.writeBytes(data, offset, len);
            byte[] pkg = bos.toByteArray();
            currentConnection.post(pkg, 0, pkg.length);
            // Log.d(TAG, "Posted message to connection #" + currentConnectionId);
        }
    }

    public static class OutMessage {
        private byte[] message;
        private int offset;
        private int len;

        public OutMessage(byte[] message, int offset, int len) {
            this.message = message;
            this.offset = offset;
            this.len = len;
        }

        public int getOffset() {
            return offset;
        }

        public int getLen() {
            return len;
        }

        public byte[] getMessage() {
            return message;
        }
    }

    public static class InMessage {
        private byte[] data;
        private int offset;
        private int len;

        public InMessage(byte[] data, int offset, int len) {
            this.data = data;
            this.offset = offset;
            this.len = len;
        }

        public byte[] getData() {
            return data;
        }

        public int getOffset() {
            return offset;
        }

        public int getLen() {
            return len;
        }
    }

    public static class NetworkChanged {
        private NetworkState state;

        public NetworkChanged(NetworkState state) {
            this.state = state;
        }
    }

    public static class ForceNetworkCheck {

    }

    private static class PerformConnectionCheck {

    }

    private static class ConnectionDie {
        private int connectionId;

        public ConnectionDie(int connectionId) {
            this.connectionId = connectionId;
        }

        public int getConnectionId() {
            return connectionId;
        }
    }

    private static class ConnectionCreateFailure {

    }

    private static class ConnectionCreated {
        private int connectionId;
        private Connection connection;

        public ConnectionCreated(int connectionId, Connection connection) {
            this.connectionId = connectionId;
            this.connection = connection;
        }

        public int getConnectionId() {
            return connectionId;
        }

        public Connection getConnection() {
            return connection;
        }
    }
}
