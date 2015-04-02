package im.actor.model.android.modules.push;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.google.android.gms.gcm.GoogleCloudMessaging;

import im.actor.model.log.Log;

import static im.actor.messenger.app.Core.messenger;

/**
 * Created by ex3ndr on 18.09.14.
 */
public class PushReceiver extends BroadcastReceiver {

    private static final String TAG = "PushReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "Received push");
        GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(context);
        Bundle extras = intent.getExtras();
        String messageType = gcm.getMessageType(intent);
        if (!extras.isEmpty()) {
            if (GoogleCloudMessaging.MESSAGE_TYPE_MESSAGE.equals(messageType)) {
                if (extras.containsKey("seq")) {
                    int seq = Integer.parseInt(extras.getString("seq"));
                    Log.d(TAG, "Received seq: " + seq);
                    messenger().onPushReceived(seq);
                } else {
                    Log.w(TAG, "Unknown message");
                }
            }
        } else {
            Log.w(TAG, "Message with null extras: ignoring");
        }
        setResultCode(Activity.RESULT_OK);
    }
}
