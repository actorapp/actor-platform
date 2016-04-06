package im.actor.sdk.controllers.profile;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import im.actor.sdk.ActorSDK;
import im.actor.sdk.R;
import im.actor.sdk.controllers.Intents;
import im.actor.sdk.controllers.activity.BaseFragmentActivity;
import im.actor.sdk.controllers.settings.BaseActorProfileActivity;

import static im.actor.sdk.util.ActorSDKMessenger.messenger;
import static im.actor.sdk.util.ActorSDKMessenger.users;

public class ProfileActivity extends BaseFragmentActivity {

    private int uid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getSupportActionBar().setDisplayShowHomeEnabled(false);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setDisplayShowCustomEnabled(false);
        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        getSupportActionBar().setTitle(null);

        uid = getIntent().getIntExtra(Intents.EXTRA_UID, 0);
        try {
            if (uid == 0) {
                uid = Integer.parseInt(getIntent().getData().getPath().replace(")", "").split("/")[2]);
            }
            users().get(uid);
        } catch (Exception e) {
            Toast.makeText(this, getString(R.string.profile_cant_find_user), Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        if (savedInstanceState == null) {
            ProfileFragment fragment;
            BaseActorProfileActivity profileIntent = ActorSDK.sharedActor().getDelegate().getProfileIntent(uid);
            if (profileIntent != null) {
                fragment = profileIntent.getProfileFragment(uid);
            } else {
                fragment = ProfileFragment.create(uid);
            }

            showFragment(fragment, false, false);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        messenger().onProfileOpen(uid);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.profile_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        } else if (item.getItemId() == R.id.edit) {
            startActivity(Intents.editUserName(uid, this));
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onPause() {
        super.onPause();
        messenger().onProfileClosed(uid);
    }
}