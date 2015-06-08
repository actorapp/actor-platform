/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.model.android.modules.call;

import com.zingaya.voximplant.VoxImplantCallback;
import com.zingaya.voximplant.VoxImplantClient;

import java.util.Map;

import im.actor.messenger.app.AppContext;
import im.actor.messenger.app.util.RandomUtil;
import im.actor.model.android.modules.CallModule;
import im.actor.model.log.Log;
import im.actor.model.modules.Modules;
import im.actor.model.modules.utils.ModuleActor;

public class CallActor extends ModuleActor {

    private static final String TAG = "CallActor";

    private CallModule callModule;

    private VoxCallback callback = new VoxCallback();
    private VoxImplantClient voxImplantClient;
    private boolean isStarted = false;
    private boolean isConnected = false;
    private boolean isLoggedIn = false;

    private long ongoingCallKey = 0;
    private String ongoingCallId = null;

    private long pendingCallKey = 0;
    private String pendingCallId = null;

    public CallActor(Modules modules, CallModule callModule) {
        super(modules);
        this.callModule = callModule;
    }

    @Override
    public void preStart() {
        super.preStart();

        voxImplantClient = VoxImplantClient.instance();
        voxImplantClient.setAndroidContext(AppContext.getContext());
        voxImplantClient.setCallback(callback);
    }

    private void startEngine() {
        if (isStarted) {
            return;
        }
        isStarted = true;

        self().send(new TryConnect());
    }

    private void tryConnect() {
        Log.d(TAG, "Connecting...");

        voxImplantClient.connect();
    }

    // Connection
    private void onVoxConnected() {
        Log.d(TAG, "Connected");
        isConnected = true;

        int myUid = modules().getAuthModule().myUid();
        Log.d(TAG, "Authenticating uid_" + myUid);
        voxImplantClient.login("uid_" + myUid + "@cloud.actor.voximplant.com", "NWU1jVPOdosiMD7tX0kn");
    }

    private void onVoxConnectionFailure() {
        Log.d(TAG, "Connection failure");

        self().sendOnce(new TryConnect(), 2000);
    }

    private void onVoxDisconnected() {
        Log.d(TAG, "Disconnected");
        isConnected = false;
        isLoggedIn = false;

        cancelOngoing();
        cancelPending();

        self().sendOnce(new TryConnect());
    }

    // Authentication
    private void onVoxLoggedIn() {
        Log.d(TAG, "Logged In");
        isLoggedIn = true;

        if (pendingCallId != null) {
            callModule.onCallConnected(pendingCallKey);
            voxImplantClient.startCall(pendingCallId);
        }
    }

    private void onVoxLogInFailure() {
        Log.d(TAG, "LogIn Failure");
    }

    // Call states

    private void tryCall(String number, long callKey) {
        cancelOngoing();
        cancelPending();

        if (isLoggedIn) {
            pendingCallKey = callKey;
            pendingCallId = voxImplantClient.createCall(number, false, number);
            callModule.onCallConnected(callKey);
            voxImplantClient.startCall(pendingCallId);
        }
    }

    private void stopCalling(long callKey) {
        if (ongoingCallKey == callKey) {
            cancelOngoing();
        }
        if (pendingCallKey == callKey) {
            cancelPending();
        }
    }

    private void answerCall(long callKey) {
        if (ongoingCallKey == callKey) {
            voxImplantClient.answerCall(ongoingCallId);
        }
    }

    // Call states
    private void onVoxCallConnected(String callId) {
        Log.d(TAG, "Call connected: " + callId);

        if (callId.equals(ongoingCallId)) {
            callModule.onCallStarted(ongoingCallKey);
        }
        if (callId.equals(pendingCallId)) {
            callStarted();
        }
    }

    private void onVoxCallStarted(String callId) {
        Log.d(TAG, "Call started: " + callId);

        if (callId.equals(ongoingCallId)) {
            callModule.onCallStarted(ongoingCallKey);
        }
        if (callId.equals(pendingCallId)) {
            callStarted();
        }
    }

    private void onVoxCallDisconnected(String callId) {
        Log.d(TAG, "Call disconnected: " + callId);

        if (callId.equals(pendingCallId)) {
            cancelPending();
        }
        if (callId.equals(ongoingCallId)) {
            cancelOngoing();
        }
    }

    private void onVoxCallRinging(String callId) {
        Log.d(TAG, "Call ringing: " + callId);
    }

    private void onVoxCallIncoming(String callId) {
        Log.d(TAG, "Incoming Call: " + callId);

        cancelPending();

        if (ongoingCallId != null) {
            // Cancel if there is active phone call
            voxImplantClient.disconnectCall(callId);
        } else {
            ongoingCallId = callId;
            ongoingCallKey = RandomUtil.randomId();
            callModule.onIncomingCall(ongoingCallKey, 0);
        }
    }

    private void onVoxCallFailure(String callId) {
        Log.d(TAG, "Call failure: " + callId);

        if (callId.equals(pendingCallId)) {
            cancelPending();
        }
        if (callId.equals(ongoingCallId)) {
            cancelOngoing();
        }
    }

    // Common methods

    private void callStarted() {
        ongoingCallKey = pendingCallKey;
        ongoingCallId = pendingCallId;
        pendingCallId = null;
        pendingCallKey = 0;
        callModule.onCallStarted(ongoingCallKey);
    }

    private void cancelPending() {
        if (pendingCallId != null) {
            voxImplantClient.disconnectCall(pendingCallId);
            callModule.onCallEnded(pendingCallKey);
            pendingCallId = null;
            pendingCallKey = 0;
        }
    }

    private void cancelOngoing() {
        if (ongoingCallId != null) {
            voxImplantClient.disconnectCall(ongoingCallId);
            callModule.onCallEnded(ongoingCallKey);
            ongoingCallId = null;
            ongoingCallKey = 0;
        }
    }

    // Messages

    @Override
    public void onReceive(Object message) {
        if (message instanceof StartEngine) {
            startEngine();
        } else if (message instanceof TryConnect) {
            tryConnect();
        } else if (message instanceof VoxLoggedIn) {
            onVoxLoggedIn();
        } else if (message instanceof VoxLogInFailure) {
            onVoxLogInFailure();
        } else if (message instanceof VoxConnectionSuccessful) {
            onVoxConnected();
        } else if (message instanceof VoxConnectionClosed) {
            onVoxDisconnected();
        } else if (message instanceof VoxConnectionFailure) {
            onVoxConnectionFailure();
        } else if (message instanceof VoxCallConnected) {
            onVoxCallConnected(((VoxCallConnected) message).getCallId());
        } else if (message instanceof VoxCallDisconnected) {
            onVoxCallDisconnected(((VoxCallDisconnected) message).getCallId());
        } else if (message instanceof VoxCallAudioStarted) {
            onVoxCallStarted(((VoxCallAudioStarted) message).getCallId());
        } else if (message instanceof VoxCallRinging) {
            onVoxCallRinging(((VoxCallRinging) message).getCallId());
        } else if (message instanceof VoxCallFailure) {
            onVoxCallFailure(((VoxCallFailure) message).getCallId());
        } else if (message instanceof PerformCall) {
            tryCall(((PerformCall) message).getNumber(), ((PerformCall) message).getRid());
        } else if (message instanceof VoxCallIncoming) {
            onVoxCallIncoming(((VoxCallIncoming) message).getCallId());
        } else if (message instanceof EndCall) {
            stopCalling(((EndCall) message).getRid());
        } else if (message instanceof AnswerCall) {
            answerCall(((AnswerCall) message).getRid());
        } else {
            drop(message);
        }
    }

    public static class StartEngine {

    }

    public static class TryConnect {

    }

    public static class PerformCall {
        private String number;
        private long rid;

        public PerformCall(String number, long rid) {
            this.number = number;
            this.rid = rid;
        }

        public String getNumber() {
            return number;
        }

        public long getRid() {
            return rid;
        }
    }

    public static class AnswerCall {
        private long rid;

        public AnswerCall(long rid) {
            this.rid = rid;
        }

        public long getRid() {
            return rid;
        }
    }

    public static class EndCall {
        private long rid;

        public EndCall(long rid) {
            this.rid = rid;
        }

        public long getRid() {
            return rid;
        }
    }

    // Vox Implant callbacks

    private class VoxLoggedIn {

    }

    private class VoxLogInFailure {

    }

    private class VoxConnectionSuccessful {

    }

    private class VoxConnectionClosed {

    }

    private class VoxConnectionFailure {

    }

    private class VoxCallConnected {
        private String callId;

        public VoxCallConnected(String callId) {
            this.callId = callId;
        }

        public String getCallId() {
            return callId;
        }
    }

    private class VoxCallDisconnected {
        private String callId;

        public VoxCallDisconnected(String callId) {
            this.callId = callId;
        }

        public String getCallId() {
            return callId;
        }
    }

    private class VoxCallRinging {
        private String callId;

        public VoxCallRinging(String callId) {
            this.callId = callId;
        }

        public String getCallId() {
            return callId;
        }
    }

    private class VoxCallFailure {
        private String callId;

        public VoxCallFailure(String callId) {
            this.callId = callId;
        }

        public String getCallId() {
            return callId;
        }
    }

    private class VoxCallAudioStarted {
        private String callId;

        public VoxCallAudioStarted(String callId) {
            this.callId = callId;
        }

        public String getCallId() {
            return callId;
        }
    }

    private class VoxCallIncoming {
        private String callId;

        public VoxCallIncoming(String callId) {
            this.callId = callId;
        }

        public String getCallId() {
            return callId;
        }
    }

    private class VoxCallback implements VoxImplantCallback {

        @Override
        public void onLoginSuccessful(String displayName) {
            self().send(new VoxLoggedIn());
        }

        @Override
        public void onLoginFailed(VoxImplantClient.LoginFailureReason loginFailureReason) {
            self().send(new VoxLogInFailure());
        }

        @Override
        public void onConnectionSuccessful() {
            self().send(new VoxConnectionSuccessful());
        }

        @Override
        public void onConnectionClosed() {
            self().send(new VoxConnectionClosed());
        }

        @Override
        public void onConnectionFailedWithError(String reason) {
            self().send(new VoxConnectionFailure());
        }

        @Override
        public void onCallConnected(String callId, Map<String, String> headers) {
            self().send(new VoxCallConnected(callId));
        }

        @Override
        public void onCallDisconnected(String callId, Map<String, String> headers) {
            self().send(new VoxCallDisconnected(callId));
        }

        @Override
        public void onCallRinging(String callId, Map<String, String> headers) {
            self().send(new VoxCallRinging(callId));
        }

        @Override
        public void onCallFailed(String callId, int code, String reason,
                                 Map<String, String> headers) {
            self().send(new VoxCallFailure(callId));
        }

        @Override
        public void onCallAudioStarted(String callId) {
            self().send(new VoxCallAudioStarted(callId));
        }

        @Override
        public void onIncomingCall(String callId, String from, String displayName, boolean videoCall,
                                   Map<String, String> headers) {
            self().send(new VoxCallIncoming(callId));
        }

        @Override
        public void onSIPInfoReceivedInCall(String callId, String type, String content,
                                            Map<String, String> headers) {

        }

        @Override
        public void onMessageReceivedInCall(String callId, String text,
                                            Map<String, String> headers) {

        }
    }
}
