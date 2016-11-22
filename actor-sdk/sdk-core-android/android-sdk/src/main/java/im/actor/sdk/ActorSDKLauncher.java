package im.actor.sdk;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import im.actor.sdk.controllers.Intents;
import im.actor.sdk.controllers.profile.ProfileActivity;

public class ActorSDKLauncher {

    /**
     * Launch User Profile Activity
     *
     * @param context current context
     * @param uid     user id
     */
    public static void startProfileActivity(Context context, int uid) {
        // Ignore call if context is empty, simple work-around when fragment was disconnected from
        // activity
        
        if (context == null) {
            return;
        }
        Bundle b = new Bundle();
        b.putInt(Intents.EXTRA_UID, uid);
        startActivity(context, b, ProfileActivity.class);
    }


    //
    // Tools
    //

    private static void startActivity(Context context, Bundle extras, Class<?> cls) {
        Intent intent = new Intent(context, cls);
        if (extras != null) {
            intent.putExtras(extras);
        }
        // Setting NEW_TASK flag for launching from background
        if (!(context instanceof Activity)) {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        }
        context.startActivity(intent);
    }
}
