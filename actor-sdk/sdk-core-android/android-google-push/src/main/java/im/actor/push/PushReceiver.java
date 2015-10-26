package im.actor.push;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.WakefulBroadcastReceiver;

import com.google.android.gms.gcm.GoogleCloudMessaging;

import im.actor.runtime.Log;
import im.actor.sdk.core.ActorPushReceiver;

public class PushReceiver extends ActorPushReceiver {

    private static final String TAG = "ActorPushReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(context);
        Bundle extras = intent.getExtras();
        String messageType = gcm.getMessageType(intent);
        if (!extras.isEmpty()) {
            if (GoogleCloudMessaging.MESSAGE_TYPE_MESSAGE.equals(messageType)) {
                if (extras.containsKey("seq")) {
                    int seq = Integer.parseInt(extras.getString("seq"));
                    onSeqReceived(seq);
                    setResultCode(Activity.RESULT_OK);
                    Log.d(TAG, "Push received");
                }
            }
        }
        WakefulBroadcastReceiver.completeWakefulIntent(intent);
    }
}
