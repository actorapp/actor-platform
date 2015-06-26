package im.actor.model.android.modules.push;

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
import im.actor.model.log.Log;

import static im.actor.messenger.app.Core.messenger;

/**
 * Created by ex3ndr on 18.09.14.
 */
public class PushReceiver extends WakefulBroadcastReceiver {

    private static final int NOTIFICATION_ID = 2;

    private static final String TAG = "PushReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "Received push");
        GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(context);
        Bundle extras = intent.getExtras();
        String messageType = gcm.getMessageType(intent);
        if (!extras.isEmpty()) {
            if (GoogleCloudMessaging.MESSAGE_TYPE_MESSAGE.equals(messageType)) {
                if (intent.getExtras().containsKey("mp_message")) {
                    String mp_message = intent.getExtras().getString("mp_message");

                    NotificationCompat.Builder builder = new NotificationCompat.Builder(context);

                    builder.setAutoCancel(true);
                    builder.setSmallIcon(R.drawable.ic_app_notify);
                    builder.setPriority(NotificationCompat.PRIORITY_HIGH);
                    builder.setCategory(NotificationCompat.CATEGORY_MESSAGE);

                    int defaults = NotificationCompat.DEFAULT_LIGHTS;
                    if (messenger().isNotificationSoundEnabled()) {
                        defaults |= NotificationCompat.DEFAULT_SOUND;
                    }
                    if (messenger().isNotificationVibrationEnabled()) {
                        defaults |= NotificationCompat.DEFAULT_VIBRATE;
                    }
                    builder.setDefaults(defaults);

                    builder.setTicker(mp_message);
                    builder.setContentTitle(context.getString(R.string.app_name));
                    builder.setContentText(mp_message);

                    builder.setContentIntent(PendingIntent.getActivity(context, 0,
                            new Intent(context, MainActivity.class),
                            PendingIntent.FLAG_UPDATE_CURRENT));

                    NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
                    manager.notify(NOTIFICATION_ID, builder.build());
                    setResultCode(Activity.RESULT_OK);
                } else {
                    if (extras.containsKey("seq")) {
                        int seq = Integer.parseInt(extras.getString("seq"));
                        Log.d(TAG, "Received seq: " + seq);
                        messenger().onPushReceived(seq);
                        setResultCode(Activity.RESULT_OK);
                    } else {
                        Log.w(TAG, "Unknown message");
                    }
                }
            }
        } else {
            Log.w(TAG, "Message with null extras: ignoring");
        }

        completeWakefulIntent(intent);
    }
}
