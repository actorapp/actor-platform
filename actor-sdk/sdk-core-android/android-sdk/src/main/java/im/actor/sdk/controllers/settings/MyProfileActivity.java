package im.actor.sdk.controllers.settings;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import im.actor.sdk.ActorSDK;
import im.actor.sdk.R;
import im.actor.sdk.controllers.Intents;
import im.actor.sdk.controllers.activity.BaseFragmentActivity;
import im.actor.sdk.controllers.fragment.preview.ViewAvatarActivity;
import im.actor.sdk.intents.ActorIntent;

import static im.actor.sdk.util.ActorSDKMessenger.myUid;

public class MyProfileActivity extends BaseFragmentActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        getSupportActionBar().setTitle(null);

        if (savedInstanceState == null) {

            BaseActorSettingsFragment fragment;
            if (ActorSDK.sharedActor().getDelegate().getSettingsIntent() != null) {
                ActorIntent settingsIntent = ActorSDK.sharedActor().getDelegate().getSettingsIntent();
                if (settingsIntent instanceof BaseActorSettingsActivity) {
                    fragment = ((BaseActorSettingsActivity) settingsIntent).getSettingsFragment();
                } else {
                    fragment = new ActorSettingsFragment();
                }
            } else {
                fragment = new ActorSettingsFragment();
            }

            showFragment(fragment, false, false);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.my_profile, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.editProfile) {
            startActivity(Intents.editMyName(this));
            return true;
        } else if (item.getItemId() == R.id.changePhoto) {
            startActivity(ViewAvatarActivity.viewAvatar(myUid(), this));
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
