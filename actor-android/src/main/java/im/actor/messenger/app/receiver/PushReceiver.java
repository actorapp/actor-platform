package im.actor.messenger.app.receiver;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import im.actor.messenger.BuildConfig;
import im.actor.messenger.core.actors.push.GooglePushActor;

/**
 * Created by ex3ndr on 18.09.14.
 */
public class PushReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (BuildConfig.ENABLE_GOOGLE_PLAY) {
            GooglePushActor.push().send(intent);
        }
        setResultCode(Activity.RESULT_OK);
    }
}
