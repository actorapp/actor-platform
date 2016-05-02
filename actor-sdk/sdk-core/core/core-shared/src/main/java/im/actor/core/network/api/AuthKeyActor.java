package im.actor.core.network.api;

import java.io.IOException;
import java.util.Random;

import im.actor.core.network.ActorApi;
import im.actor.core.network.Endpoints;
import im.actor.core.network.TrustedKey;
import im.actor.core.network.mtp.entity.ProtoMessage;
import im.actor.core.network.mtp.entity.ProtoPackage;
import im.actor.core.network.mtp.entity.ProtoSerializer;
import im.actor.core.network.mtp.entity.ProtoStruct;
import im.actor.core.network.mtp.entity.RequestDH;
import im.actor.core.network.mtp.entity.RequestGetServerKey;
import im.actor.core.network.mtp.entity.RequestStartAuth;
import im.actor.core.network.mtp.entity.ResponseDoDH;
import im.actor.core.network.mtp.entity.ResponseGetServerKey;
import im.actor.core.network.mtp.entity.ResponseStartAuth;
import im.actor.core.util.ExponentialBackoff;
import im.actor.runtime.Crypto;
import im.actor.runtime.Log;
import im.actor.runtime.Network;
import im.actor.runtime.actors.Actor;
import im.actor.runtime.actors.ActorRef;
import im.actor.runtime.actors.Cancellable;
import im.actor.runtime.bser.DataInput;
import im.actor.runtime.crypto.Cryptos;
import im.actor.runtime.crypto.Curve25519;
import im.actor.runtime.crypto.Curve25519KeyPair;
import im.actor.runtime.crypto.Digest;
import im.actor.runtime.crypto.primitives.prf.PRF;
import im.actor.runtime.crypto.primitives.util.ByteStrings;
import im.actor.runtime.mtproto.Connection;
import im.actor.runtime.mtproto.ConnectionCallback;
import im.actor.runtime.mtproto.CreateConnectionCallback;

// Disabling Bounds checks for speeding up calculations

/*-[
#define J2OBJC_DISABLE_ARRAY_BOUND_CHECKS 1
]-*/

public class AuthKeyActor extends Actor {

    private static final String TAG = "AuthKeyActor";

    private ActorRef parentActor;
    private Endpoints endpoints;
    private Connection connection;
    private int connectionId = 0;
    private long randomId;
    private Random random = new Random();
    private final ExponentialBackoff exponentialBackoff = new ExponentialBackoff(1000, 30000, 25);
    private ActorState currentState;
    private Cancellable reconnectCancellable;

    private void startKeyCreation(Endpoints endpoints) {
        Log.d(TAG, "startKeyCreation");
        if (sender() == null) {
            return;
        }

        if (connection != null) {
            connection.close();
            connection = null;
        }
        this.parentActor = sender();
        this.endpoints = endpoints;
        this.randomId = random.nextLong();
        this.exponentialBackoff.reset();

        goToStartState();

        if (reconnectCancellable != null) {
            reconnectCancellable.cancel();
            reconnectCancellable = null;
        }
        reconnectCancellable = schedule(new TryCreateConnection(), 0);
    }

    //
    // Message Processing
    //

    private void onConnectionStarted() {
        try {
            if (currentState == null) {
                throw new IOException();
            }

            ProtoStruct struct = currentState.sendStartMessage();
            byte[] data = new ProtoPackage(0, 0, new ProtoMessage(0, struct.toByteArray())).toByteArray();
            connection.post(data, 0, data.length);
        } catch (Exception e) {
            e.printStackTrace();
            crashConnection();
        }
    }

    private void onMessage(int connectionId, byte[] data, int offset, int len) {
        if (connectionId != this.connectionId) {
            Log.d(TAG, "Too old: ignoring");
            return;
        }

        ProtoStruct protoStruct;
        try {
            DataInput dataInput = new DataInput(data, offset, len);
            ProtoPackage protoPackage = new ProtoPackage(dataInput);
            if (protoPackage.getAuthId() != 0) {
                throw new IOException("AuthId != 0");
            }
            if (protoPackage.getSessionId() != 0) {
                throw new IOException("Session != 0");
            }
            if (protoPackage.getPayload().getMessageId() != 0) {
                throw new IOException("MessageId != 0");
            }
            protoStruct = ProtoSerializer.readMessagePayload(protoPackage.getPayload().getPayload());
        } catch (IOException e) {
            e.printStackTrace();
            crashConnection();
            return;
        }

        try {
            if (currentState == null) {
                throw new IOException();
            }
            currentState.onMessage(protoStruct);
        } catch (Exception e) {
            e.printStackTrace();
            crashConnection();
        }
    }

    private void goToStartState() {
        goToState(new ActorState() {
            @Override
            public ProtoStruct sendStartMessage() throws IOException {
                Log.d(TAG, "Sending RequestStartAuth");
                return new RequestStartAuth(randomId);
            }

            @Override
            public void onMessage(ProtoStruct struct) throws IOException {
                if (struct instanceof ResponseStartAuth) {
                    Log.d(TAG, "Received ResponseStartAuth");
                    ResponseStartAuth startAuth = (ResponseStartAuth) struct;
                    if (startAuth.getRandomId() != randomId) {
                        throw new IOException("Incorrect RandomId");
                    }
                    long[] keys = startAuth.getAvailableKeys();

                    if (keys.length == 0) {
                        throw new IOException("No keys installed on server. Please, configure your server correctly.");
                    }

                    if (endpoints.getTrustedKeys().length == 0) {
                        gotoKeyDownloadState(keys[0], startAuth.getServerNonce());
                    } else {
                        for (long l : keys) {
                            for (TrustedKey tk : endpoints.getTrustedKeys()) {
                                if (tk.getKeyId() == l) {
                                    if (tk.getKey() != null) {
                                        gotoDHState(tk.getKeyId(), tk.getKey(), startAuth.getServerNonce());
                                    } else {
                                        gotoKeyDownloadState(tk.getKeyId(), startAuth.getServerNonce());
                                    }
                                    return;
                                }
                            }
                        }
                        throw new IOException("No trusted keys found!");
                    }
                } else {
                    throw new IOException("Expected: ResponseStartAuth, got: " + struct.getClass().getName());
                }
            }
        });
    }

    private void gotoKeyDownloadState(final long keyId, final byte[] serverNonce) {
        goToState(new ActorState() {
            @Override
            public ProtoStruct sendStartMessage() throws IOException {
                Log.d(TAG, "Sending RequestGetServerKey");
                return new RequestGetServerKey(keyId);
            }

            @Override
            public void onMessage(ProtoStruct struct) throws IOException {
                if (struct instanceof ResponseGetServerKey) {
                    Log.d(TAG, "Received ResponseGetServerKey");
                    ResponseGetServerKey r = (ResponseGetServerKey) struct;
                    if (r.getKeyId() != keyId) {
                        throw new IOException("Incorrect KeyId");
                    }
                    gotoDHState(keyId, r.getKey(), serverNonce);
                } else {
                    throw new IOException("Expected: ResponseGetServerKey, got: " + struct.getClass().getName());
                }
            }
        });
    }

    private void gotoDHState(final long keyId, final byte[] key, final byte[] serverNonce) {
        final byte[] clientNonce = new byte[32];
        Crypto.nextBytes(clientNonce);
        byte[] keyMaterial = new byte[32];
        Crypto.nextBytes(keyMaterial);
        final Curve25519KeyPair clientKeyPair = Curve25519.keyGen(keyMaterial);

        goToState(new ActorState() {
            @Override
            public ProtoStruct sendStartMessage() throws IOException {
                Log.d(TAG, "Sending RequestDH");
                return new RequestDH(randomId, keyId, clientNonce, clientKeyPair.getPublicKey());
            }

            @Override
            public void onMessage(ProtoStruct struct) throws IOException {
                if (struct instanceof ResponseDoDH) {
                    Log.d(TAG, "Received ResponseDoDH");
                    ResponseDoDH r = (ResponseDoDH) struct;
                    if (r.getRandomId() != randomId) {
                        throw new IOException("Incorrect RandomId");
                    }

                    PRF combinedPrf = Cryptos.PRF_SHA_STREEBOG_256();
                    byte[] nonce = ByteStrings.merge(clientNonce, serverNonce);
                    byte[] pre_master_secret = Curve25519.calculateAgreement(clientKeyPair.getPrivateKey(), key);
                    byte[] master_secret = combinedPrf.calculate(pre_master_secret, "master secret", nonce, 256);
                    byte[] verify = combinedPrf.calculate(master_secret, "client finished", nonce, 256);
                    if (!Curve25519.verifySignature(key, verify, r.getVerifySign())) {
                        throw new IOException("Incorrect Signature");
                    }
                    Digest sha256 = Crypto.createSHA256();
                    sha256.update(master_secret, 0, master_secret.length);
                    byte[] authIdHash = new byte[32];
                    sha256.doFinal(authIdHash, 0);
                    long authId = ByteStrings.bytesToLong(authIdHash);

                    Log.d(TAG, "Key successfully created #" + authId);

                    gotoSuccess(master_secret, authId);
                } else {
                    throw new IOException("Expected: ResponseGetServerKey, got: " + struct.getClass().getName());
                }
            }
        });
    }

    private void gotoSuccess(byte[] masterKey, long authId) {
        parentActor.send(new KeyCreated(authId, masterKey));
        crashConnectionAndState();
    }

    private void goToState(ActorState state) {
        currentState = state;
        if (connection != null) {
            try {
                ProtoStruct struct = currentState.sendStartMessage();
                byte[] data = new ProtoPackage(0, 0, new ProtoMessage(0, struct.toByteArray())).toByteArray();
                connection.post(data, 0, data.length);
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    }

    //
    // Connection Management
    //

    private void tryCreateConnection() {
        Log.d(TAG, "Trying to connect...");
        final int currentConnection = ++connectionId;
        Network.createConnection(currentConnection,
                ActorApi.MTPROTO_VERSION,
                ActorApi.API_MAJOR_VERSION,
                ActorApi.API_MINOR_VERSION,
                endpoints.fetchEndpoint(false),
                new ConnectionCallback() {
                    @Override
                    public void onConnectionRedirect(String host, int port, int timeout) {
                        // As Not Supported
                        self().send(new AuthKeyActor.OnConnectionDie(currentConnection));
                    }

                    @Override
                    public void onMessage(byte[] data, int offset, int len) {
                        self().send(new AuthKeyActor.OnMessage(currentConnection, data, offset, len));
                    }

                    @Override
                    public void onConnectionDie() {
                        self().send(new AuthKeyActor.OnConnectionDie(currentConnection));
                    }
                }, new CreateConnectionCallback() {
                    @Override
                    public void onConnectionCreated(Connection connection) {
                        AuthKeyActor.this.onConnectionCreated(currentConnection, connection);
                    }

                    @Override
                    public void onConnectionCreateError() {
                        self().send(new AuthKeyActor.OnConnectionDie(currentConnection));
                    }
                });
    }

    private void onConnectionCreated(int connectionId, Connection connection) {
        Log.d(TAG, "onConnectionCreated");
        if (connectionId != this.connectionId) {
            connection.close();
            Log.d(TAG, "Too old: ignoring");
            return;
        }
        exponentialBackoff.onSuccess();
        this.connection = connection;
        onConnectionStarted();
    }

    private void onConnectionDie(int connectionId) {
        Log.d(TAG, "onConnectionDie");
        if (connectionId != this.connectionId) {
            Log.d(TAG, "Too old: ignoring");
            return;
        }

        crashConnection();

        exponentialBackoff.onFailure();
        if (currentState != null) {
            long delay = exponentialBackoff.exponentialWait();
            Log.d(TAG, "Trying to recreate connection in " + delay + " ms...");
            if (reconnectCancellable != null) {
                reconnectCancellable.cancel();
                reconnectCancellable = null;
            }
            reconnectCancellable = schedule(new TryCreateConnection(), delay);
        }
    }

    private void crashConnectionAndState() {
        Log.d(TAG, "Crashing state...");
        currentState = null;
        crashConnection();
    }

    private void crashConnection() {
        Log.d(TAG, "Crashing connection");
        this.connectionId++;
        if (this.connection != null) {
            this.connection.close();
            this.connection = null;
        }
    }

    //
    // Actor Messages
    //

    @Override
    public void onReceive(Object message) {
        if (message instanceof StartKeyCreation) {
            startKeyCreation(((StartKeyCreation) message).getEndpoints());
        } else if (message instanceof OnConnectionDie) {
            onConnectionDie(((OnConnectionDie) message).getConnectionId());
        } else if (message instanceof OnConnectionCreated) {
            onConnectionCreated(((OnConnectionCreated) message).getConnectionId(),
                    ((OnConnectionCreated) message).getConnection());
        } else if (message instanceof OnMessage) {
            onMessage(((OnMessage) message).getConnectionId(),
                    ((OnMessage) message).getData(),
                    ((OnMessage) message).getOffset(),
                    ((OnMessage) message).getLength());
        } else if (message instanceof TryCreateConnection) {
            tryCreateConnection();
        } else {
            super.onReceive(message);
        }
    }

    private static class OnConnectionDie {
        private int connectionId;

        public OnConnectionDie(int connectionId) {
            this.connectionId = connectionId;
        }

        public int getConnectionId() {
            return connectionId;
        }
    }

    private static class OnConnectionCreated {
        private int connectionId;
        private Connection connection;

        public OnConnectionCreated(int connectionId, Connection connection) {
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

    private static class OnMessage {
        private int connectionId;
        private byte[] data;
        private int offset;
        private int length;

        public OnMessage(int connectionId, byte[] data, int offset, int length) {
            this.connectionId = connectionId;
            this.data = data;
            this.offset = offset;
            this.length = length;
        }

        public int getConnectionId() {
            return connectionId;
        }

        public byte[] getData() {
            return data;
        }

        public int getOffset() {
            return offset;
        }

        public int getLength() {
            return length;
        }
    }

    public static class StartKeyCreation {
        private Endpoints endpoints;

        public StartKeyCreation(Endpoints endpoints) {
            this.endpoints = endpoints;
        }

        public Endpoints getEndpoints() {
            return endpoints;
        }
    }

    public static class KeyCreated {

        private long authKeyId;

        private byte[] authKey;

        public KeyCreated(long authKeyId, byte[] authKey) {
            this.authKeyId = authKeyId;
            this.authKey = authKey;
        }

        public long getAuthKeyId() {
            return authKeyId;
        }

        public byte[] getAuthKey() {
            return authKey;
        }
    }

    private class TryCreateConnection {

    }

    private interface ActorState {

        ProtoStruct sendStartMessage() throws IOException;

        void onMessage(ProtoStruct struct) throws IOException;
    }
}