package im.actor.sdk.controllers.settings;

import android.os.Bundle;

import im.actor.sdk.ActorSDK;
import im.actor.sdk.R;
import im.actor.sdk.controllers.activity.BaseFragmentActivity;
import im.actor.sdk.intents.ActorIntent;
import im.actor.sdk.intents.ActorIntentFragmentActivity;

public class ChatSettingsActivity extends BaseFragmentActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getSupportActionBar().setTitle(R.string.settings_chat_title);

        ChatSettingsFragment fragment;
        ActorIntent chatSettingsIntent = ActorSDK.sharedActor().getDelegate().getChatSettingsIntent();
        if (chatSettingsIntent != null && chatSettingsIntent instanceof ActorIntentFragmentActivity) {
            fragment = (ChatSettingsFragment) ((ActorIntentFragmentActivity) chatSettingsIntent).getFragment();
        } else {
            fragment = new ChatSettingsFragment();
        }

        if (savedInstanceState == null) {
            showFragment(fragment, false, false);
        }
    }
}
