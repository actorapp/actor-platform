package im.actor.messenger.app.fragment.profile;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import im.actor.core.viewmodel.UserVM;
import im.actor.messenger.R;
import im.actor.messenger.app.Intents;
import im.actor.messenger.app.activity.BaseFragmentActivity;
import im.actor.messenger.app.activity.MainActivity;
import im.actor.runtime.mvvm.ValueChangedListener;
import im.actor.runtime.mvvm.Value;

import static im.actor.messenger.app.core.Core.messenger;
import static im.actor.messenger.app.core.Core.users;

/**
 * Created by ex3ndr on 12.09.14.
 */
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
            showFragment(ProfileFragment.create(uid), false, false);
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
            Intent i = new Intent(this, MainActivity.class);
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
