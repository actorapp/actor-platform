package im.actor.core.modules.internal;

import java.util.HashMap;

import im.actor.core.api.ApiOutPeer;
import im.actor.core.api.ApiPeerType;
import im.actor.core.api.rpc.RequestCallInProgress;
import im.actor.core.api.rpc.RequestDoCall;
import im.actor.core.api.rpc.RequestEndCall;
import im.actor.core.api.rpc.RequestSendCallSignal;
import im.actor.core.api.rpc.RequestSubscribeToCalls;
import im.actor.core.api.rpc.ResponseDoCall;
import im.actor.core.entity.User;
import im.actor.core.entity.signals.AbsSignal;
import im.actor.core.modules.AbsModule;
import im.actor.core.modules.ModuleContext;
import im.actor.core.modules.events.IncomingCall;
import im.actor.core.modules.events.NewSessionCreated;
import im.actor.core.modules.internal.calls.CallActor;
import im.actor.core.network.RpcCallback;
import im.actor.core.network.RpcException;
import im.actor.core.viewmodel.Command;
import im.actor.core.viewmodel.CommandCallback;
import im.actor.runtime.Log;
import im.actor.runtime.actors.ActorCreator;
import im.actor.runtime.actors.ActorRef;
import im.actor.runtime.actors.ActorSystem;
import im.actor.runtime.actors.Props;
import im.actor.runtime.eventbus.BusSubscriber;
import im.actor.runtime.eventbus.Event;

public class CallsModule extends AbsModule {

    public static final int MAX_CALLS_COUNT = 1;
    private static final String TAG = "CALLS";

    public CallsModule(ModuleContext context) {
        super(context);
    }


    public static final int CALL_TIMEOUT = 10;
    public static boolean CALLS_ENABLED = false;
    public static boolean MULTIPLE_CALLS_ENABLED = false;
    HashMap<Long, ActorRef> calls = new HashMap<Long, ActorRef>();

    public void run() {
        if (CALLS_ENABLED) {
            request(new RequestSubscribeToCalls());
            context().getEvents().subscribe(new BusSubscriber() {
                @Override
                public void onBusEvent(Event event) {
                    request(new RequestSubscribeToCalls());
                }
            }, NewSessionCreated.EVENT);
        }
    }

    public Command<ResponseDoCall> makeCall(final int uid, final CallCallback callCallback) {
        return new Command<ResponseDoCall>() {
            @Override
            public void start(final CommandCallback<ResponseDoCall> callback) {
                User u = users().getValue(uid);
                request(new RequestDoCall(new ApiOutPeer(ApiPeerType.PRIVATE, u.getUid(), u.getAccessHash()), CALL_TIMEOUT), new RpcCallback<ResponseDoCall>() {
                    @Override
                    public void onResult(final ResponseDoCall response) {
                        callback.onResult(response);

                        Log.d(TAG, "make call " + response.getCallId());
                        calls.put(response.getCallId(),
                                ActorSystem.system().actorOf(Props.create(CallActor.class, new ActorCreator<CallActor>() {
                                    @Override
                                    public CallActor create() {
                                        return new CallActor(response.getCallId(), callCallback, context());
                                    }
                                }), "actor/call_" + response.getCallId()));


                    }

                    @Override
                    public void onError(RpcException e) {
                        callback.onError(e);
                    }
                });
            }
        };
    }

    public void callInProgress(long callId) {
        request(new RequestCallInProgress(callId, CALL_TIMEOUT));
    }

    public void handleCall(final long callId, final CallCallback callback) {
        ActorRef call = calls.get(callId);
        if (call != null) {
            call.send(new CallActor.HandleCall(callback));
        } else {
            //can't find call - close fragment
            callback.onCallEnd();
        }
    }

    //do end call
    public void endCall(long callId) {
        Log.d(TAG, "do end call" + callId);

        request(new RequestEndCall(callId));
        ActorRef call = calls.get(callId);
        if (call != null) {
            Log.d(TAG, "call exist - end it");

            call.send(new CallActor.EndCall());
        } else {
            Log.d(TAG, "call not exist - remove it");
            onCallEnded(callId);
        }
    }

    public void onIncomingCall(final long callId, int uid) {
        Log.d(TAG, "incoming call " + callId);

        if (!calls.keySet().contains(callId)) {
            calls.put(callId,
                    ActorSystem.system().actorOf(Props.create(CallActor.class, new ActorCreator<CallActor>() {
                        @Override
                        public CallActor create() {
                            return new CallActor(callId, context());
                        }
                    }), "actor/call_" + callId));
            if (!MULTIPLE_CALLS_ENABLED & calls.keySet().size() > MAX_CALLS_COUNT) {
                calls.get(callId).send(new CallActor.EndCall());
            } else {
                context().getEvents().post(new IncomingCall(callId, uid));
            }
        }

    }

    //on end call update
    public void onEndCall(long callId) {
        Log.d(TAG, "end call update: " + callId);
        ActorRef call = calls.get(callId);
        if (call != null) {
            Log.d(TAG, "call exist - end it");
            call.send(new CallActor.EndCall());
        } else {
            Log.d(TAG, "call not exist - remove it");
            calls.remove(callId);
        }
    }

    //after end call update processed by CallActor
    public void onCallEnded(long callId) {
        Log.d(TAG, "on callActor ended call: " + callId);
        calls.remove(callId);
    }

    public void onCallInProgress(long callId, int timeout) {
        ActorRef call = calls.get(callId);
        if (call != null) {
            call.send(new CallActor.CallInProgress(timeout));
        }
    }

    public void sendSignal(long callId, AbsSignal signal) {
        request(new RequestSendCallSignal(callId, signal.toByteArray()));
    }

    public void onSignal(long callId, byte[] data) {
        ActorRef call = calls.get(callId);
        if (call != null) {
            call.send(new CallActor.Signal(data));
        }
    }

    public interface CallCallback {
        void onCallEnd();

        void onSignal(byte[] data);
    }
}
