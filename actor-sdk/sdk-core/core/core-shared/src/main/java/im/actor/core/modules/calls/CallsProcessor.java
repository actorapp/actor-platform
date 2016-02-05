package im.actor.core.modules.calls;

import im.actor.core.api.updates.UpdateCallEnded;
import im.actor.core.api.updates.UpdateCallInProgress;
import im.actor.core.api.updates.UpdateCallSignal;
import im.actor.core.api.updates.UpdateIncomingCall;
import im.actor.core.modules.AbsModule;
import im.actor.core.modules.ModuleContext;

public class CallsProcessor extends AbsModule {
    public CallsProcessor(ModuleContext context) {
        super(context);
    }

    public void onIncomingCall(UpdateIncomingCall call) {
        context().getCallsModule().onIncomingCall(call.getCallId(), call.getUid());
    }

    public void onCallInProgress(UpdateCallInProgress inProgress) {
        context().getCallsModule().onCallInProgress(inProgress.getCallId(), inProgress.getTimeout());
    }

    public void onCallEnd(UpdateCallEnded callEnd) {
        context().getCallsModule().onEndCall(callEnd.getCallId());
    }

    public void onSignal(UpdateCallSignal signal) {
        context().getCallsModule().onSignal(signal.getCallId(), signal.getContent());
    }
}
