package im.actor.sdk.core;

import android.support.v4.content.WakefulBroadcastReceiver;

import im.actor.sdk.ActorSDK;

public abstract class ActorPushReceiver extends WakefulBroadcastReceiver {
    public void onSeqReceived(int seq) {
        ActorSDK.sharedActor().getMessenger().onPushReceived(seq);
    }
}
