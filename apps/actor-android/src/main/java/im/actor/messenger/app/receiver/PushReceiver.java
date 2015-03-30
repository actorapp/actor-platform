package im.actor.messenger.app.receiver;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * Created by ex3ndr on 18.09.14.
 */
public class PushReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {

        setResultCode(Activity.RESULT_OK);
    }
}
