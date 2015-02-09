package im.actor.model.network.mtp.actors;

import com.droidkit.actors.*;
import com.droidkit.actors.conf.EnvConfig;
import com.droidkit.actors.utils.AtomicIntegerCompat;
import im.actor.model.network.ConnectionFactory;
import im.actor.model.log.Log;
import im.actor.model.network.Connection;
import im.actor.model.network.ConnectionCallback;
import im.actor.model.network.Endpoints;
import im.actor.model.network.mtp.MTProto;
import im.actor.model.network.mtp.entity.ProtoMessage;
import im.actor.model.util.DataInput;
import im.actor.model.util.DataOutput;
import im.actor.model.util.ExponentialBackoff;

import java.io.IOException;

/**
 * Created by ex3ndr on 02.09.14.
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
                }), mtProto.getActorPath() + "/manager"));
    }

    private static final AtomicIntegerCompat NEXT_CONNECTION = EnvConfig.createAtomicInt(1);

    private final MTProto mtProto;
    private final Endpoints endpoints;
    private final long authId;
    private final long sessionId;

    // Connection
    private int currentConnectionId;
    private Connection currentConnection;

    // Creating
    private boolean isCheckingConnections = false;
    private final ExponentialBackoff backoff = new ExponentialBackoff();

    private ActorRef receiver;
    private ActorRef sender;

    public ManagerActor(MTProto mtProto) {
        this.mtProto = mtProto;
        this.endpoints = mtProto.getEndpoints();
        this.authId = mtProto.getAuthId();
        this.sessionId = mtProto.getSessionId();
    }

    @Override
    public void preStart() {
        receiver = ReceiverActor.receiver(mtProto);
        sender = SenderActor.senderActor(mtProto);
        checkConnection();
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
            onNetworkChanged();
        }
        // Messages
        else if (message instanceof OutMessage) {
            OutMessage m = (OutMessage) message;
            onOutMessage(m.message, m.offset, m.len);
        } else if (message instanceof InMessage) {
            InMessage m = (InMessage) message;
            onInMessage(m.data, m.offset, m.len);
        }
    }

    private void onConnectionCreated(int id, Connection connection) {
        Log.d(TAG, "Connection #" + id + " created");

        if (connection.isClosed()) {
            Log.w(TAG, "Unable to register connection #" + id + ": already closed");
            return;
        }

        if (currentConnectionId == id) {
            Log.w(TAG, "Unable to register connection #" + id + ": already have connection");
            return;
        }

        if (currentConnection != null) {
            currentConnection.close();
            currentConnectionId = 0;
        }

        currentConnectionId = id;
        currentConnection = connection;


        backoff.onSuccess();
        isCheckingConnections = false;
        requestCheckConnection();

        sender.send(new SenderActor.ConnectionCreated());
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
            requestCheckConnection();
        } else {
            Log.w(TAG, "Unable to unregister connection #" + id + ": connection not found");
        }
    }

    private void onNetworkChanged() {
        Log.w(TAG, "Network configuration changed");

        backoff.reset();
        checkConnection();
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
            Log.d(TAG, "Trying to create connection...");

            isCheckingConnections = true;

            final int id = NEXT_CONNECTION.getAndIncrement();
            ConnectionFactory.createConnection(id, endpoints.fetchEndpoint(), new ConnectionCallback() {
                @Override
                public void onMessage(byte[] data, int offset, int len) {
                    self().send(new InMessage(data, offset, len));
                }

                @Override
                public void onConnectionDie() {
                    self().send(new ConnectionDie(id));
                }
            }, new ConnectionFactory.CreateConnectionCallback() {
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
            }
        }
    }

    private void onOutMessage(byte[] data, int offset, int len) {
        // Log.d(TAG, "Out message");

        // Cleanup bad connection
        if (currentConnection != null && currentConnection.isClosed()) {
            currentConnection = null;
            currentConnectionId = 0;
        }

        if (currentConnection != null) {
            DataOutput bos = new DataOutput();
            bos.writeLong(authId);
            bos.writeLong(sessionId);
            bos.writeBytes(data, offset, len);
            byte[] pkg = bos.toByteArray();
            currentConnection.post(pkg, 0, pkg.length);
            // Log.d(TAG, "Posted message to connection #" + currentConnectionId);
        } else {
            // Log.d(TAG, "Unable to send message: no connections");
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
