/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.core.network.api;

import java.io.IOException;
import java.util.HashMap;

import im.actor.core.api.parser.RpcParser;
import im.actor.runtime.*;
import im.actor.runtime.Runtime;
import im.actor.runtime.actors.Actor;
import im.actor.runtime.actors.ActorCreator;
import im.actor.runtime.actors.ActorRef;
import im.actor.runtime.actors.ActorSystem;
import im.actor.runtime.actors.Props;
import im.actor.core.modules.utils.RandomUtils;
import im.actor.core.network.ActorApiCallback;
import im.actor.core.network.AuthKeyStorage;
import im.actor.core.network.Endpoints;
import im.actor.core.network.NetworkState;
import im.actor.core.network.RpcCallback;
import im.actor.core.network.RpcException;
import im.actor.core.network.RpcInternalException;
import im.actor.core.network.mtp.AuthIdRetriever;
import im.actor.core.network.mtp.MTProto;
import im.actor.core.network.mtp.MTProtoCallback;
import im.actor.core.network.mtp.entity.ProtoSerializer;
import im.actor.core.network.mtp.entity.ProtoStruct;
import im.actor.core.network.mtp.entity.rpc.Push;
import im.actor.core.network.mtp.entity.rpc.RpcError;
import im.actor.core.network.mtp.entity.rpc.RpcFloodWait;
import im.actor.core.network.mtp.entity.rpc.RpcInternalError;
import im.actor.core.network.mtp.entity.rpc.RpcOk;
import im.actor.core.network.mtp.entity.rpc.RpcRequest;
import im.actor.core.network.parser.Request;
import im.actor.core.network.parser.Response;
import im.actor.core.network.parser.RpcScope;
import im.actor.runtime.threading.AtomicIntegerCompat;
import im.actor.runtime.threading.AtomicLongCompat;
import im.actor.core.util.ExponentialBackoff;

public class ApiBroker extends Actor {

    public static ActorRef get(final Endpoints endpoints, final AuthKeyStorage keyStorage, final ActorApiCallback callback,
                               final boolean isEnableLog, int id, final int minDelay,
                               final int maxDelay,
                               final int maxFailureCount) {
        return ActorSystem.system().actorOf(Props.create(ApiBroker.class, new ActorCreator<ApiBroker>() {
            @Override
            public ApiBroker create() {
                return new ApiBroker(endpoints, keyStorage, callback, isEnableLog, minDelay,
                        maxDelay,
                        maxFailureCount);
            }
        }).changeDispatcher("network"), "api/broker#" + id);
    }

    private static final String TAG = "ApiBroker";
    private static final AtomicLongCompat NEXT_RPC_ID = im.actor.runtime.Runtime.createAtomicLong(1);
    private static final AtomicIntegerCompat NEXT_PROTO_ID = im.actor.runtime.Runtime.createAtomicInt(1);

    private final Endpoints endpoints;
    private final AuthKeyStorage keyStorage;
    private final ActorApiCallback callback;
    private final boolean isEnableLog;
    private final int minDelay;
    private final int maxDelay;
    private final int maxFailureCount;

    private final HashMap<Long, RequestHolder> requests = new HashMap<Long, RequestHolder>();
    private final HashMap<Long, Long> idMap = new HashMap<Long, Long>();

    private long currentAuthId;
    private MTProto proto;

    private ExponentialBackoff authIdBackOff;

    public ApiBroker(Endpoints endpoints, AuthKeyStorage keyStorage,
                     ActorApiCallback callback,
                     boolean isEnableLog, int minDelay, int maxDelay, int maxFailureCount) {
        this.isEnableLog = isEnableLog;
        this.endpoints = endpoints;
        this.keyStorage = keyStorage;
        this.callback = callback;
        this.minDelay = minDelay;
        this.maxDelay = maxDelay;
        this.maxFailureCount = maxFailureCount;
        authIdBackOff = new ExponentialBackoff(minDelay, maxDelay, maxFailureCount);
    }

    @Override
    public void preStart() {
        this.currentAuthId = keyStorage.getAuthKey();
        if (currentAuthId == 0) {
            self().send(new RequestAuthId());
        } else {
            if (isEnableLog) {
                Log.d(TAG, "Key loaded: " + currentAuthId);
            }
            self().send(new InitMTProto(currentAuthId));
        }
    }

    @Override
    public void postStop() {
        if (proto != null) {
            proto.stopProto();
            proto = null;
        }
    }

    private void onNetworkChanged(NetworkState state) {
        if (proto != null) {
            proto.onNetworkChanged(state);
        }
    }

    private void forceNetworkCheck() {
        if (proto != null) {
            proto.forceNetworkCheck();
        }
    }

    private void onNewSessionCreated(long authId) {
        if (authId != currentAuthId) {
            return;
        }

        Log.w(TAG, "New Session Created");

        callback.onNewSessionCreated();
    }

    private void onAuthIdInvalidated(long authId) {
        if (authId != currentAuthId) {
            return;
        }

        Log.w(TAG, "Auth id invalidated");

        keyStorage.saveAuthKey(0);
        currentAuthId = 0;
        proto = null;

        callback.onAuthIdInvalidated();

        self().send(new RequestAuthId());
    }

    private void requestAuthId() {
        Log.d(TAG, "Creating auth key...");

        AuthIdRetriever.requestAuthId(endpoints, minDelay, maxDelay, maxFailureCount, new AuthIdRetriever.AuthIdCallback() {
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
        currentAuthId = key;

        proto = new MTProto(key,
                RandomUtils.nextRid(),
                endpoints,
                new ProtoCallback(key),
                isEnableLog,
                getPath() + "/proto#" + NEXT_PROTO_ID.incrementAndGet(),
                minDelay,
                maxDelay,
                maxFailureCount);

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
                Runtime.getCurrentTime(),
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

    private void processResponse(long authId, long mid, byte[] content) {
        if (authId != currentAuthId) {
            return;
        }

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

            Log.d(TAG, "<- response#" + holder.publicId + ": " + response +
                    " in " + (Runtime.getCurrentTime() - holder.requestTime) + " ms");

            holder.callback.onResult(response);
        } else if (protoStruct instanceof RpcError) {
            RpcError e = (RpcError) protoStruct;
            requests.remove(rid);
            if (holder.protoId != 0) {
                idMap.remove(holder.protoId);
            }

            Log.w(TAG, "<- error#" + holder.publicId + ": " + e.errorTag + " " + e.errorCode + " " + e.userMessage
                    + " in " + (Runtime.getCurrentTime() - holder.requestTime) + " ms");

            holder.callback.onError(new RpcException(e.errorTag, e.errorCode, e.userMessage, e.canTryAgain, e.relatedData));
        } else if (protoStruct instanceof RpcInternalError) {
            RpcInternalError e = ((RpcInternalError) protoStruct);
            Log.d(TAG, "<- internal_error#" + holder.publicId + " " + e.getTryAgainDelay() + " sec" +
                    " in " + (Runtime.getCurrentTime() - holder.requestTime) + " ms");
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
            Log.d(TAG, "<- flood_wait#" + holder.publicId + " " + f.getDelay() + " sec" +
                    " in " + (Runtime.getCurrentTime() - holder.requestTime) + " ms");
            self().send(new ForceResend(rid), f.getDelay() * 1000L);
        } else {
            Log.d(TAG, "<- unknown_package#" + holder.publicId +
                    " in " + (Runtime.getCurrentTime() - holder.requestTime) + " ms");
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

    private void processUpdate(long authId, byte[] content) {
        if (authId != currentAuthId) {
            return;
        }

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

    void connectionCountChanged(int count) {
        callback.onConnectionsChanged(count);
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

        public NetworkState getState() {
            return state;
        }
    }

    public static class ForceNetworkCheck {

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

        private long authId;
        private long responseId;
        private byte[] data;

        public ProtoResponse(long authId, long responseId, byte[] data) {
            this.authId = authId;
            this.responseId = responseId;
            this.data = data;
        }

        public long getAuthId() {
            return authId;
        }

        public long getResponseId() {
            return responseId;
        }

        public byte[] getData() {
            return data;
        }
    }

    private class ProtoUpdate {

        private long authId;
        private byte[] data;

        public ProtoUpdate(long authId, byte[] data) {
            this.authId = authId;
            this.data = data;
        }

        public long getAuthId() {
            return authId;
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
        private final long requestTime;
        private final RpcRequest message;
        private final long publicId;
        private final RpcCallback callback;

        private long protoId;

        private RequestHolder(long requestTime, long publicId, RpcRequest message, RpcCallback callback) {
            this.requestTime = requestTime;
            this.message = message;
            this.publicId = publicId;
            this.callback = callback;
        }
    }

    private class NewSessionCreated {
        private long authId;

        public NewSessionCreated(long authId) {
            this.authId = authId;
        }

        public long getAuthId() {
            return authId;
        }
    }

    private class AuthIdInvalidated {
        private long authId;

        public AuthIdInvalidated(long authId) {
            this.authId = authId;
        }

        public long getAuthId() {
            return authId;
        }
    }

    private class ConnectionsCountChanged {
        private int count;

        public ConnectionsCountChanged(int count) {
            this.count = count;
        }

        public int getCount() {
            return count;
        }
    }

    private class ProtoCallback implements MTProtoCallback {

        private long authId;

        public ProtoCallback(long authId) {
            this.authId = authId;
        }

        @Override
        public void onRpcResponse(long mid, byte[] content) {
            self().send(new ProtoResponse(authId, mid, content));
        }

        @Override
        public void onUpdate(byte[] content) {
            self().send(new ProtoUpdate(authId, content));
        }

        @Override
        public void onAuthKeyInvalidated(long authId) {
            if (this.authId != authId) {
                // But why??
                return;
            }

            self().send(new AuthIdInvalidated(authId));
        }

        @Override
        public void onSessionCreated() {
            self().send(new NewSessionCreated(authId));
        }

        @Override
        public void onConnectionsCountChanged(int count) {
            self().send(new ConnectionsCountChanged(count));
        }
    }

    @Override
    public void onReceive(Object message) {
        if (message instanceof RequestAuthId) {
            requestAuthId();
        } else if (message instanceof InitMTProto) {
            InitMTProto initMTProto = (InitMTProto) message;
            createMtProto(initMTProto.getAuthId());
        } else if (message instanceof PerformRequest) {
            PerformRequest request = (PerformRequest) message;
            performRequest(NEXT_RPC_ID.getAndIncrement(),
                    request.getMessage(), request.getCallback());
        } else if (message instanceof CancelRequest) {
            CancelRequest cancelRequest = (CancelRequest) message;
            cancelRequest(cancelRequest.getRandomId());
        } else if (message instanceof ProtoResponse) {
            ProtoResponse response = (ProtoResponse) message;
            processResponse(response.getAuthId(), response.getResponseId(), response.getData());
        } else if (message instanceof ForceResend) {
            ForceResend forceResend = (ForceResend) message;
            forceResend(forceResend.getId());
        } else if (message instanceof ProtoUpdate) {
            ProtoUpdate update = (ProtoUpdate) message;
            processUpdate(update.getAuthId(), update.getData());
        } else if (message instanceof NewSessionCreated) {
            NewSessionCreated newSessionCreated = (NewSessionCreated) message;
            onNewSessionCreated(newSessionCreated.getAuthId());
        } else if (message instanceof AuthIdInvalidated) {
            AuthIdInvalidated authIdInvalidated = (AuthIdInvalidated) message;
            onAuthIdInvalidated(authIdInvalidated.getAuthId());
        } else if (message instanceof NetworkChanged) {
            NetworkChanged networkChanged = (NetworkChanged) message;
            onNetworkChanged(networkChanged.getState());
        } else if (message instanceof ForceNetworkCheck) {
            forceNetworkCheck();
        } else if (message instanceof ConnectionsCountChanged) {
            connectionCountChanged(((ConnectionsCountChanged) message).getCount());
        } else {
            drop(message);
        }
    }
}