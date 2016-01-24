package im.actor.sdk.receivers;

import android.util.Log;

import im.actor.sdk.push.ActorPushReceiver;

public class SDKPushReceiver extends ActorPushReceiver {

    @Override
    public void onPushReceived(String payload) {
        Log.d("TestPushReceiver", "On push received: " + payload);
    }
}