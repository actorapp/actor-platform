package im.actor.sdk.core;

import android.content.Context;
import android.content.Intent;

import im.actor.core.providers.CallsProvider;
import im.actor.core.viewmodel.CallState;
import im.actor.sdk.ActorSDK;
import im.actor.sdk.controllers.calls.CallActivity;

import static im.actor.sdk.util.ActorSDKMessenger.messenger;

public class AndroidCallProvider implements CallsProvider {

    @Override
    public void onCallStart(long callId) {
        Context context = ActorSDK.sharedActor().getMessenger().getContext();
        Intent callIntent = new Intent(context, CallActivity.class);
        callIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_MULTIPLE_TASK | Intent.FLAG_ACTIVITY_NEW_DOCUMENT);
        callIntent.putExtra("callId", callId);
        context.startActivity(callIntent);
        context.startActivity(callIntent);
    }

    @Override
    public void onCallEnd(long callId) {
        messenger().getCall(callId).getState().change(CallState.ENDED);
    }

    @Override
    public void startOutgoingBeep() {
        // TODO: Implement
    }

    @Override
    public void stopOutgoingBeep() {
        // TODO: Implement
    }
}
