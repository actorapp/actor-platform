package im.actor.sdk.controllers.fragment.profile;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import im.actor.core.viewmodel.UserVM;
import im.actor.sdk.ActorSDK;
import im.actor.sdk.R;
import im.actor.sdk.controllers.Intents;
import im.actor.sdk.controllers.activity.ActorMainActivity;
import im.actor.sdk.controllers.activity.BaseFragmentActivity;
import im.actor.runtime.mvvm.ValueChangedListener;
import im.actor.runtime.mvvm.Value;
import im.actor.sdk.controllers.fragment.settings.ActorSettingsFragment;
import im.actor.sdk.controllers.fragment.settings.BaseActorProfileActivity;
import im.actor.sdk.controllers.fragment.settings.BaseActorSettingsFragment;

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
            if (uid == 0)
                uid = Integer.parseInt(getIntent().getData().getPath().replace(")", "").split("/")[2]);
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
        bind(users().get(uid).isContact(), new ValueChangedListener<Boolean>() {
            @Override
            public void onChanged(Boolean val, Value<Boolean> Value) {
                invalidateOptionsMenu();
            }
        });
        messenger().onProfileOpen(uid);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        try {
            getMenuInflater().inflate(R.menu.profile_menu, menu);
            UserVM userVM = users().get(uid);
            if (userVM.isBot()) {
                menu.findItem(R.id.remove).setVisible(false);
                menu.findItem(R.id.add).setVisible(false);
                menu.findItem(R.id.share).setVisible(false);
            } else {
                if (userVM.isContact().get()) {
                    menu.findItem(R.id.remove).setVisible(true);
                    menu.findItem(R.id.add).setVisible(false);
                } else {
                    menu.findItem(R.id.remove).setVisible(false);
                    menu.findItem(R.id.add).setVisible(true);
                }
                menu.findItem(R.id.share).setVisible(false);
            }
        } catch (RuntimeException e) {
            // Toast made OnCreate
        }

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        } else if (item.getItemId() == R.id.add) {
            execute(messenger().addContact(uid), R.string.profile_adding);
            return true;
        } else if (item.getItemId() == R.id.remove) {
            execute(messenger().removeContact(uid), R.string.profile_removing);
            return true;
        } else if (item.getItemId() == R.id.edit) {
            startActivity(Intents.editUserName(uid, this));
        } else if (item.getItemId() == R.id.share) {
            Intent i = new Intent(this, ActorMainActivity.class);
            i.putExtra("share_user", uid);
            startActivity(i);
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onPause() {
        super.onPause();
        messenger().onProfileClosed(uid);
    }
}
