package im.actor.core.modules.calls;

import im.actor.core.api.ApiOffer;
import im.actor.core.api.ApiWebRTCSignaling;
import im.actor.core.api.rpc.RequestDoCall;
import im.actor.core.api.rpc.ResponseDoCall;
import im.actor.core.entity.Peer;
import im.actor.core.modules.ModuleContext;
import im.actor.core.util.RandomUtils;
import im.actor.runtime.Log;
import im.actor.runtime.WebRTC;
import im.actor.runtime.function.Consumer;
import im.actor.runtime.function.Function;
import im.actor.runtime.promise.Promise;
import im.actor.runtime.webrtc.WebRTCLocalStream;
import im.actor.runtime.webrtc.WebRTCPeerConnection;

public class CallMasterActor extends CallActor {

    private static final String TAG = "CallMasterActor";

    private final Peer peer;

    public CallMasterActor(Peer peer, ModuleContext context) {
        super(context);
        this.peer = peer;
    }

    @Override
    public void onBusCreated() {
        Log.d(TAG, "onBusCreated");
        api(new RequestDoCall(buidOutPeer(peer), getBusId())).then(new Consumer<ResponseDoCall>() {
            @Override
            public void apply(ResponseDoCall responseDoCall) {
                Log.d(TAG, "onBusCreated:result");
                onCallCreated();
            }
        }).failure(new Consumer<Exception>() {
            @Override
            public void apply(Exception e) {
                Log.d(TAG, "onBusCreated:error");
                dispose();
            }
        }).done(self());
    }

    public void onCallCreated() {

    }

    @Override
    public void onDeviceConnected(final int uid, final long deviceId) {
        Log.d(TAG, "onDeviceConnected");
        final long sessionId = RandomUtils.nextRid();
        WebRTC.createPeerConnection().mapPromiseSelf(new Function<WebRTCPeerConnection, Promise<WebRTCLocalStream>>() {
            @Override
            public Promise<WebRTCLocalStream> apply(final WebRTCPeerConnection webRTCPeerConnection) {
                return WebRTC.getUserAudio().then(new Consumer<WebRTCLocalStream>() {
                    @Override
                    public void apply(WebRTCLocalStream stream) {
                        webRTCPeerConnection.addOwnStream(stream);
                    }
                });
            }
        }).mapPromiseSelf(new Function<WebRTCPeerConnection, Promise<Boolean>>() {
            @Override
            public Promise<Boolean> apply(final WebRTCPeerConnection webRTCPeerConnection) {
                return webRTCPeerConnection.createOffer().then(new Consumer<String>() {
                    @Override
                    public void apply(String sdp) {
                        sendSignalingMessage(uid, deviceId, new ApiOffer(sessionId, sdp));
                    }
                }).mapPromise(new Function<String, Promise<Boolean>>() {
                    @Override
                    public Promise<Boolean> apply(String s) {
                        return webRTCPeerConnection.setLocalDescription("offer", s);
                    }
                });
            }
        }).then(new Consumer<WebRTCPeerConnection>() {
            @Override
            public void apply(WebRTCPeerConnection webRTCPeerConnection) {
                Log.d(TAG, "onCallCreated:then");
            }
        }).failure(new Consumer<Exception>() {
            @Override
            public void apply(Exception e) {
                Log.d(TAG, "onCallCreated:failure");
            }
        }).done(self());
    }

    @Override
    public void onDeviceDisconnected(int uid, long deviceId) {
        Log.d(TAG, "onDeviceDisconnected");
    }

    @Override
    public void onSignalingMessage(int fromUid, long fromDeviceId, ApiWebRTCSignaling signaling) {
        Log.d(TAG, "onSignalingMessage");
    }

    @Override
    public void onBusShutdown() {
        Log.d(TAG, "onBusShutdown");
    }

    @Override
    public void onBusDisposed() {
        Log.d(TAG, "onBusDisposed");
    }

    @Override
    public void onBusStopped() {
        Log.d(TAG, "onBusStopped");
    }
}