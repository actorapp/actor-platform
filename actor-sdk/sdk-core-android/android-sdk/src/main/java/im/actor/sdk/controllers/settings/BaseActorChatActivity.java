package im.actor.sdk.controllers.settings;

import android.content.Intent;

import im.actor.core.entity.Peer;
import im.actor.sdk.controllers.conversation.MessagesFragment;
import im.actor.sdk.intents.ActorIntentFragmentActivity;

public abstract class BaseActorChatActivity extends ActorIntentFragmentActivity {
    public BaseActorChatActivity(Intent intent) {
        super(intent);
    }

    public BaseActorChatActivity(Intent intent, MessagesFragment fragment) {
        super(intent, fragment);
    }

    public BaseActorChatActivity() {
        super();
    }

    public MessagesFragment getChatFragment(Peer peer) {
        return null;
    }
}
