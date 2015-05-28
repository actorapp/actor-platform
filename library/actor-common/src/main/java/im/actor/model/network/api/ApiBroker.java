/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.model.network.api;

import java.io.IOException;
import java.util.HashMap;
import java.util.Random;

import im.actor.model.NetworkProvider;
import im.actor.model.api.parser.RpcParser;
import im.actor.model.droidkit.actors.Actor;
import im.actor.model.droidkit.actors.ActorCreator;
import im.actor.model.droidkit.actors.ActorRef;
import im.actor.model.droidkit.actors.ActorSystem;
import im.actor.model.droidkit.actors.Environment;
import im.actor.model.droidkit.actors.Props;
import im.actor.model.log.Log;
import im.actor.model.network.ActorApiCallback;
import im.actor.model.network.AuthKeyStorage;
import im.actor.model.network.Endpoints;
import im.actor.model.network.NetworkState;
import im.actor.model.network.RpcCallback;
import im.actor.model.network.RpcException;
import im.actor.model.network.RpcInternalException;
import im.actor.model.network.mtp.AuthIdRetriever;
import im.actor.model.network.mtp.MTProto;
import im.actor.model.network.mtp.MTProtoCallback;
import im.actor.model.network.mtp.entity.ProtoSerializer;
import im.actor.model.network.mtp.entity.ProtoStruct;
import im.actor.model.network.mtp.entity.rpc.Push;
import im.actor.model.network.mtp.entity.rpc.RpcError;
import im.actor.model.network.mtp.entity.rpc.RpcFloodWait;
import im.actor.model.network.mtp.entity.rpc.RpcInternalError;
import im.actor.model.network.mtp.entity.rpc.RpcOk;
import im.actor.model.network.mtp.entity.rpc.RpcRequest;
import im.actor.model.network.parser.Request;
import im.actor.model.network.parser.Response;
import im.actor.model.network.parser.RpcScope;
import im.actor.model.util.AtomicLongCompat;
import im.actor.model.util.ExponentialBackoff;

public class ApiBroker extends Actor {

    public static ActorRef get(final Endpoints endpoints, final AuthKeyStorage keyStorage, final ActorApiCallback callback,
                               final NetworkProvider networkProvider, final boolean isEnableLog) {
        return ActorSystem.system().actorOf(Props.create(ApiBroker.class, new ActorCreator<ApiBroker>() {
            @Override
            public ApiBroker create() {
                return new ApiBroker(endpoints, keyStorage, callback, networkProvider, isEnableLog);
            }
        }), "api/broker");
    }

    private static final String TAG = "ApiBroker";
    private static final AtomicLongCompat NEXT_RPC_ID = Environment.createAtomicLong(1);

    private final Endpoints endpoints;
    private final AuthKeyStorage keyStorage;
    private final ActorApiCallback callback;
    private final boolean isEnableLog;

    private final HashMap<Long, RequestHolder> requests = new HashMap<Long, RequestHolder>();
    private final HashMap<Long, Long> idMap = new HashMap<Long, Long>();

    private MTProto proto;

    private NetworkProvider networkProvider;
    private ExponentialBackoff authIdBackOff = new ExponentialBackoff();

    public ApiBroker(Endpoints endpoints, AuthKeyStorage keyStorage, ActorApiCallback callback,
                     NetworkProvider networkProvider, boolean isEnableLog) {
        this.isEnableLog = isEnableLog;
        this.endpoints = endpoints;
        this.keyStorage = keyStorage;
        this.callback = callback;
        this.networkProvider = networkProvider;
    }

    @Override
    public void preStart() {
        if (keyStorage.getAuthKey() == 0) {
            self().send(new RequestAuthId());
        } else {
            if (isEnableLog) {
                Log.d(TAG, "Key loaded: " + keyStorage.getAuthKey());
            }
            self().send(new InitMTProto(keyStorage.getAuthKey()));
        }
    }

    @Override
    public void onReceive(Object message) {
        if (message instanceof RequestAuthId) {
            requestAuthId();
        } else if (message instanceof InitMTProto) {
            createMtProto(((InitMTProto) message).getAuthId());
        } else if (message instanceof PerformRequest) {
            performRequest(
                    NEXT_RPC_ID.getAndIncrement(),
                    ((PerformRequest) message).getMessage(),
                    ((PerformRequest) message).getCallback());
        } else if (message instanceof CancelRequest) {
            cancelRequest(((CancelRequest) message).getRandomId());
        } else if (message instanceof ProtoResponse) {
            processResponse(((ProtoResponse) message).getResponseId(), ((ProtoResponse) message).getData());
        } else if (message instanceof ForceResend) {
            forceResend(((ForceResend) message).id);
        } else if (message instanceof ProtoUpdate) {
            processUpdate(((ProtoUpdate) message).getData());
        } else if (message instanceof NetworkChanged) {
            onNetworkChanged(((NetworkChanged) message).state);
        }
    }

    private void onNetworkChanged(NetworkState state) {
        if (proto != null) {
            proto.onNetworkChanged(state);
        }
    }

    private void requestAuthId() {
        Log.d(TAG, "Creating auth key...");

        AuthIdRetriever.requestAuthId(endpoints, networkProvider, new AuthIdRetriever.AuthIdCallback() {
            @Override
            public void onSuccess(long authId) {
                Log.d(TAG, "Key created: " + authId);
                self().send(new InitMTProto(authId));
            }

            @Override
            public void onFailure() {
                Log.d(TAG, "Key creation failure");
                authIdBackOff.onFailure();
                long delay = authIdBackOff.exponentialWait();
                Log.d(TAG, "Key creation delay in " + delay + " ms");
                self().send(new RequestAuthId(), delay);
            }
        });
    }

    private void createMtProto(long key) {
        Log.d(TAG, "Creating proto");
        keyStorage.saveAuthKey(key);
        proto = new MTProto(key, new Random().nextLong(), endpoints,
                new MTProtoCallback() {
                    @Override
                    public void onRpcResponse(long mid, byte[] content) {
                        self().send(new ProtoResponse(mid, content));
                    }

                    @Override
                    public void onUpdate(byte[] content) {
                        self().send(new ProtoUpdate(content));
                    }

                    @Override
                    public void onAuthKeyInvalidated(long authKey) {
                        callback.onAuthIdInvalidated(authKey);
                    }

                    @Override
                    public void onSessionCreated() {
                        callback.onNewSessionCreated();
                    }
                }, networkProvider, isEnableLog);

        for (RequestHolder holder : requests.values()) {
            holder.protoId = proto.sendRpcMessage(holder.message);
            idMap.put(holder.protoId, holder.publicId);
            // Log.d(TAG, holder.message + " rid#" + holder.publicId + " <- mid#" + holder.protoId);
        }
    }

    private void performRequest(long randomId, Request message, RpcCallback callback) {
        Log.d(TAG, "-> request#" + randomId + ": " + message);
        // Log.d(TAG, message + " rid#" + randomId);
        RequestHolder holder = new RequestHolder(
                randomId,
                new RpcRequest(message.getHeaderKey(), message.toByteArray()),
                callback);
        requests.put(holder.publicId, holder);

        if (proto != null) {
            long mid = proto.sendRpcMessage(holder.message);
            holder.protoId = mid;
            idMap.put(mid, randomId);
            // Log.d(TAG, message + " rid#" + randomId + " <- mid#" + mid);
        }
    }

    private void processResponse(long mid, byte[] content) {
        ProtoStruct protoStruct;
        try {
            protoStruct = ProtoSerializer.readRpcResponsePayload(content);
        } catch (IOException e) {
            e.printStackTrace();
            Log.w(TAG, "Broken response mid#" + mid);
            return;
        }

        // Log.w(TAG, protoStruct + " mid#" + mid);

        long rid;
        if (idMap.containsKey(mid)) {
            rid = idMap.get(mid);
        } else {
            return;
        }

        RequestHolder holder;
        if (requests.containsKey(rid)) {
            holder = requests.get(rid);
        } else {
            return;
        }

        if (protoStruct instanceof RpcOk) {
            RpcOk ok = (RpcOk) protoStruct;
            requests.remove(rid);
            if (holder.protoId != 0) {
                idMap.remove(holder.protoId);
            }

            Response response;
            try {
                response = (Response) new RpcParser().read(ok.responseType, ok.payload);
            } catch (IOException e) {
                e.printStackTrace();
                return;
            }

            Log.d(TAG, "<- response#" + holder.publicId + ": " + response);

            holder.callback.onResult(response);
        } else if (protoStruct instanceof RpcError) {
            RpcError e = (RpcError) protoStruct;
            requests.remove(rid);
            if (holder.protoId != 0) {
                idMap.remove(holder.protoId);
            }

            Log.w(TAG, "<- error#" + holder.publicId + ": " + e.errorTag + " " + e.errorCode + " " + e.userMessage);

            holder.callback.onError(new RpcException(e.errorTag, e.errorCode, e.userMessage, e.canTryAgain, e.relatedData));
        } else if (protoStruct instanceof RpcInternalError) {
            RpcInternalError e = ((RpcInternalError) protoStruct);
            Log.d(TAG, "<- internal_error#" + holder.publicId + " " + e.getTryAgainDelay() + " sec");
            if (e.isCanTryAgain()) {
                self().send(new ForceResend(rid), e.getTryAgainDelay() * 1000L);
            } else {
                requests.remove(rid);
                if (holder.protoId != 0) {
                    idMap.remove(holder.protoId);
                }
                holder.callback.onError(new RpcInternalException());
            }
        } else if (protoStruct instanceof RpcFloodWait) {
            RpcFloodWait f = (RpcFloodWait) protoStruct;
            Log.d(TAG, "<- flood_wait#" + holder.publicId + " " + f.getDelay() + " sec");
            self().send(new ForceResend(rid), f.getDelay() * 1000L);
        } else {
            Log.d(TAG, "<- unknown_package#" + holder.publicId);
        }
    }

    private void forceResend(long randomId) {
        RequestHolder holder = requests.get(randomId);
        if (holder != null) {
            if (holder.protoId != 0) {
                idMap.remove(holder.protoId);
                proto.cancelRpc(holder.protoId);
            }
            long mid = proto.sendRpcMessage(holder.message);
            holder.protoId = mid;
            idMap.put(mid, randomId);
        }
    }

    private void cancelRequest(long randomId) {
        RequestHolder holder = requests.get(randomId);
        if (holder != null) {
            requests.remove(randomId);
            if (holder.protoId != 0 && proto != null) {
                idMap.remove(holder.protoId);
                proto.cancelRpc(holder.protoId);
            }
        }
    }

    private void processUpdate(byte[] content) {
        ProtoStruct protoStruct;
        try {
            protoStruct = ProtoSerializer.readUpdate(content);
        } catch (IOException e) {
            e.printStackTrace();
            Log.w(TAG, "Broken mt update");
            return;
        }

        int type = ((Push) protoStruct).updateType;
        byte[] body = ((Push) protoStruct).body;

        RpcScope updateBox;
        try {
            updateBox = new RpcParser().read(type, body);
        } catch (IOException e) {
            e.printStackTrace();
            Log.w(TAG, "Broken update box");
            return;
        }

        // Log.w(TAG, "Box: " + updateBox + "");

        callback.onUpdateReceived(updateBox);
    }

    public static class PerformRequest {
        private Request message;
        private RpcCallback callback;

        public PerformRequest(Request message, RpcCallback callback) {
            this.message = message;
            this.callback = callback;
        }

        public Request getMessage() {
            return message;
        }

        public RpcCallback getCallback() {
            return callback;
        }
    }

    public static class CancelRequest {
        private long randomId;

        public CancelRequest(long randomId) {
            this.randomId = randomId;
        }

        public long getRandomId() {
            return randomId;
        }
    }

    public static class NetworkChanged {
        private NetworkState state;

        public NetworkChanged(NetworkState state) {
            this.state = state;
        }
    }

    private class RequestAuthId {

    }

    private class InitMTProto {
        private long authId;

        public InitMTProto(long authId) {
            this.authId = authId;
        }

        public long getAuthId() {
            return authId;
        }
    }

    private class ProtoResponse {
        private long responseId;
        private byte[] data;

        public ProtoResponse(long responseId, byte[] data) {
            this.responseId = responseId;
            this.data = data;
        }

        public long getResponseId() {
            return responseId;
        }

        public byte[] getData() {
            return data;
        }
    }

    private class ProtoUpdate {
        private byte[] data;

        public ProtoUpdate(byte[] data) {
            this.data = data;
        }

        public byte[] getData() {
            return data;
        }
    }

    private class ForceResend {
        private long id;

        public ForceResend(long id) {
            this.id = id;
        }

        public long getId() {
            return id;
        }
    }

    private class RequestHolder {
        private final RpcRequest message;
        private final long publicId;
        private final RpcCallback callback;

        private long protoId;

        private RequestHolder(long publicId, RpcRequest message, RpcCallback callback) {
            this.message = message;
            this.publicId = publicId;
            this.callback = callback;
        }
    }


}