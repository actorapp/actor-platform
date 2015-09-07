package im.actor.messenger;

import android.app.Activity;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.WakefulBroadcastReceiver;

import com.google.android.gms.gcm.GoogleCloudMessaging;

import im.actor.messenger.R;
import im.actor.messenger.app.activity.MainActivity;
import im.actor.runtime.Log;


import static im.actor.messenger.app.core.Core.messenger;

public class PushReceiver extends WakefulBroadcastReceiver {

    private static final String TAG = "PushReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(context);
        Bundle extras = intent.getExtras();
        String messageType = gcm.getMessageType(intent);
        if (!extras.isEmpty()) {
            if (GoogleCloudMessaging.MESSAGE_TYPE_MESSAGE.equals(messageType)) {
                if (extras.containsKey("seq")) {
                    int seq = Integer.parseInt(extras.getString("seq"));
                    messenger().onPushReceived(seq);
                    setResultCode(Activity.RESULT_OK);
                    Log.d(TAG, "Push received");
                }
            }
        }
        completeWakefulIntent(intent);
    }
}
