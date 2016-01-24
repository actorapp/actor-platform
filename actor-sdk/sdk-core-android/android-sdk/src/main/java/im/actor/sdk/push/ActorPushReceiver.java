package im.actor.sdk.push;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

/**
 * Abstract class for Actor Push receiver
 */
public abstract class ActorPushReceiver extends BroadcastReceiver {

    public ActorPushReceiver() {

    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if ("im.actor.push.intent.RECEIVE".equals(intent.getAction())) {
            onPushReceived(intent.getStringExtra("push_payload"));
        } else {
            // Ignore
        }
    }

    /**
     * Called when push is received
     *
     * @param payload payload of the push
     */
    public abstract void onPushReceived(String payload);
}