package im.actor.sdk.intents;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import im.actor.sdk.ActorSDK;
import im.actor.sdk.controllers.activity.ActorMainActivity;
import im.actor.sdk.controllers.auth.AuthActivity;

@Deprecated
public class ActivityManager {

    public void startAuthActivity(Context context) {
        startAuthActivity(context, null);
    }

    public void startAuthActivity(Context context, Bundle extras) {
        if (!startDelegateActivity(context, ActorSDK.sharedActor().getDelegate().getAuthStartIntent(), extras)) {
            startActivity(context, extras, AuthActivity.class);
        }
    }

    public void startAfterLoginActivity(Context context) {
        startAfterLoginActivity(context, null);
    }

    public void startAfterLoginActivity(Context context, Bundle extras) {
        if (!startDelegateActivity(context, ActorSDK.sharedActor().getDelegate().getAuthStartIntent(), extras)) {
            startActivity(context, extras, ActorMainActivity.class);
        }
    }

    public void startMessagingActivity(Context context) {
        startMessagingActivity(context, null);
    }

    public void startMessagingActivity(Context context, Bundle extras) {
        if (!startDelegateActivity(context, ActorSDK.sharedActor().getDelegate().getAuthStartIntent(), extras)) {
            startActivity(context, extras, ActorMainActivity.class);
        }
    }

    private boolean startDelegateActivity(Context context, ActorIntent intent, Bundle extras) {
        if (intent != null && intent instanceof ActorIntentActivity) {
            Intent startIntent = ((ActorIntentActivity) ActorSDK.sharedActor().getDelegate().getAuthStartIntent()).getIntent();
            if (extras != null) {
                startIntent.putExtras(extras);
            }
            context.startActivity(startIntent);
            return true;
        } else {
            return false;
        }

    }

    private void startActivity(Context context, Bundle extras, Class<?> cls) {
        Intent intent = new Intent(context, cls);
        if (extras != null) {
            intent.putExtras(extras);
        }
        context.startActivity(intent);
    }


}
