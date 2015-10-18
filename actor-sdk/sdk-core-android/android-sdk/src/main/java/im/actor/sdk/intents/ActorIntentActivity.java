package im.actor.sdk.intents;

import android.content.Intent;

public class ActorIntentActivity extends ActorIntent {

    private Intent intent;

    public ActorIntentActivity(Intent intent) {
        this.intent = intent;
    }

    public Intent getIntent() {
        return intent;
    }
}
