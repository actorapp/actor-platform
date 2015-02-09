package im.actor.model.network.mtp;

import im.actor.model.network.ConnectionFactory;
import im.actor.model.log.Log;
import im.actor.model.network.Connection;
import im.actor.model.network.ConnectionCallback;
import im.actor.model.network.Endpoints;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

import static im.actor.model.util.StreamingUtils.*;

/**
 * Created by ex3ndr on 07.02.15.
 */
public class AuthIdRetriever {

    private static final String TAG = "AuthId";

    public static void requestAuthId(Endpoints endpoints, final AuthIdCallback callback) {
        Log.d(TAG, "Requesting AuthId");

        final boolean[] isFinished = new boolean[1];
        isFinished[0] = false;

        ConnectionFactory.createConnection(0, endpoints.fetchEndpoint(), new ConnectionCallback() {
            @Override
            public void onMessage(byte[] data, int offset, int len) {
                if (isFinished[0]) {
                    return;
                }

                try {
                    ByteArrayInputStream bis = new ByteArrayInputStream(data, offset, len);

                    long reqAuthId = readLong(bis);
                    long sessionId = readLong(bis);
                    long messageId = readLong(bis);
                    byte[] content = readProtoBytes(bis);
                    int header = content[0] & 0xFF;
                    long authId = readLong(content, 1);

                    if (!isFinished[0]) {
                        isFinished[0] = true;
                        callback.onSuccess(authId);

                        Log.d(TAG, "Auth Id loaded: " + authId);

                        return;
                    }
                } catch (Exception e) {
                    Log.d(TAG, "Error during parsing auth id response");
                    e.printStackTrace();
                }

                // Hack for aborting connection
                throw new RuntimeException();
            }

            @Override
            public void onConnectionDie() {
                if (!isFinished[0]) {
                    isFinished[0] = true;
                    callback.onFailure();

                    Log.d(TAG, "Connection dies");
                }
            }
        }, new ConnectionFactory.CreateConnectionCallback() {
            @Override
            public void onConnectionCreated(Connection connection) {
                if (isFinished[0]) {
                    return;
                }

                Log.d(TAG, "Connection created");

                try {
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    writeLong(0, baos);// AuthId
                    writeLong(0, baos);// SessionId
                    writeLong(0, baos); // MessageId

                    writeVarInt(1, baos);// Payload Size
                    writeByte(0xF0, baos);// Payload: Request AuthId header
                    byte[] data = baos.toByteArray();
                    connection.post(data, 0, data.length);
                } catch (Exception e) {
                    Log.d(TAG, "Error during requesting auth id");
                    e.printStackTrace();
                    if (!isFinished[0]) {
                        isFinished[0] = true;
                        callback.onFailure();
                    }
                }
            }

            @Override
            public void onConnectionCreateError() {
                if (!isFinished[0]) {
                    isFinished[0] = true;
                    callback.onFailure();
                    Log.d(TAG, "Unable to create connection");
                }
            }
        });
    }

    public static interface AuthIdCallback {
        void onSuccess(long authId);

        void onFailure();
    }
}