package im.actor.core.modules.calls;

import im.actor.core.api.ApiOutPeer;
import im.actor.core.api.ApiPeerType;
import im.actor.core.api.rpc.RequestDoCall;
import im.actor.core.entity.User;
import im.actor.core.network.RpcCallback;
import im.actor.core.network.RpcException;
import im.actor.core.viewmodel.CommandCallback;
import im.actor.core.webrtc.WebRTCProvider;
import im.actor.core.api.rpc.ResponseDoCall;
import im.actor.core.modules.AbsModule;
import im.actor.core.modules.ModuleContext;
import im.actor.core.viewmodel.Command;
import im.actor.runtime.actors.ActorRef;

import static im.actor.runtime.actors.ActorSystem.system;

public class CallsModule extends AbsModule {

    public static final int CALL_TIMEOUT = 10;

    public static final String TAG = "CALLS";

    private WebRTCProvider provider;
    private ActorRef callManager;

    public CallsModule(ModuleContext context) {
        super(context);

        provider = context().getConfiguration().getWebRTCProvider();
    }

    public void run() {
        if (provider == null) {
            return;
        }

        callManager = system().actorOf("calls/manager", CallManagerActor.CONSTRUCTOR(context()));
    }

    public ActorRef getCallManager() {
        return callManager;
    }

    public Command<ResponseDoCall> makeCall(final int uid) {
        return new Command<ResponseDoCall>() {
            @Override
            public void start(final CommandCallback<ResponseDoCall> callback) {
                User u = users().getValue(uid);
                request(new RequestDoCall(new ApiOutPeer(ApiPeerType.PRIVATE, u.getUid(), u.getAccessHash()), CALL_TIMEOUT), new RpcCallback<ResponseDoCall>() {
                    @Override
                    public void onResult(final ResponseDoCall response) {
                        callManager.send(new CallManagerActor.OnOutgoingCall(response.getCallId(), uid));
                        callback.onResult(response);
                    }

                    @Override
                    public void onError(RpcException e) {
                        callback.onError(e);
                    }
                });
            }
        };
    }
}
