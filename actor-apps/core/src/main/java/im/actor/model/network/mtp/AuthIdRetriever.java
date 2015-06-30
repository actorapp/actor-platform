/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.model.network.mtp;

import im.actor.model.log.Log;
import im.actor.model.network.ActorApi;
import im.actor.model.network.Connection;
import im.actor.model.network.ConnectionCallback;
import im.actor.model.network.CreateConnectionCallback;
import im.actor.model.network.Endpoints;
import im.actor.model.NetworkProvider;
import im.actor.model.droidkit.bser.DataInput;
import im.actor.model.droidkit.bser.DataOutput;
import im.actor.model.util.ExponentialBackoff;

public class AuthIdRetriever {

    private static final String TAG = "AuthId";

    public static void requestAuthId(Endpoints endpoints, NetworkProvider networkProvider, int minDelay,
                                     int maxDelay, int maxFailureCount, final AuthIdCallback callback) {
        Log.d(TAG, "Requesting AuthId");

        final boolean[] isFinished = new boolean[1];
        final ExponentialBackoff backoff = new ExponentialBackoff(maxDelay, minDelay, maxFailureCount);
        isFinished[0] = false;

        networkProvider.createConnection(0,
                ActorApi.MTPROTO_VERSION,
                ActorApi.API_MAJOR_VERSION,
                ActorApi.API_MINOR_VERSION,
                endpoints.fetchEndpoint(), new ConnectionCallback() {

                    @Override
                    public void onConnectionRedirect(String host, int port, int timeout) {
                        if (!isFinished[0]) {
                            isFinished[0] = true;
                            callback.onFailure();
                            Log.d(TAG, "Unable to create connection");
                        }
                    }

                    @Override
                    public void onMessage(byte[] data, int offset, int len) {
                        if (isFinished[0]) {
                            return;
                        }

                        try {
                            DataInput dataInput = new DataInput(data, offset, len);
                            long pAuthId = dataInput.readLong();
                            long pSessionId = dataInput.readLong();
                            long messageId = dataInput.readLong();
                            byte[] payload = dataInput.readProtoBytes();

                            DataInput msg = new DataInput(payload, 0, payload.length);
                            int header = msg.readByte();
                            long authId = msg.readLong();

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
                }, new CreateConnectionCallback() {
                    @Override
                    public void onConnectionCreated(Connection connection) {
                        if (isFinished[0]) {
                            return;
                        }

                        Log.d(TAG, "Connection created");
                        backoff.onSuccess();

                        try {
                            DataOutput output = new DataOutput();
                            output.writeLong(0); // AuthId
                            output.writeLong(0); // SessionId
                            output.writeLong(0); // MessageId
                            output.writeVarInt(1); // Payload Size
                            output.writeByte(0xF0); // Payload: Request AuthId header
                            byte[] data = output.toByteArray();
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