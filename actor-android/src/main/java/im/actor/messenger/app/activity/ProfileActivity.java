package im.actor.messenger.app.activity;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import im.actor.messenger.R;
import im.actor.messenger.app.Intents;
import im.actor.messenger.app.base.BaseFragmentActivity;
import im.actor.messenger.app.fragment.profile.ProfileFragment;
import im.actor.model.mvvm.ValueChangedListener;
import im.actor.model.mvvm.ValueModel;
import im.actor.model.viewmodel.UserVM;

import static im.actor.messenger.core.Core.messenger;
import static im.actor.messenger.core.Core.users;

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

        uid = getIntent().getIntExtra(Intents.EXTRA_UID, 0);

        if (savedInstanceState == null) {
            showFragment(ProfileFragment.create(uid), false, false);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        bind(users().get(uid).isContact(), new ValueChangedListener<Boolean>() {
            @Override
            public void onChanged(Boolean val, ValueModel<Boolean> valueModel) {
                invalidateOptionsMenu();
            }
        });
        messenger().onProfileOpen(uid);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.profile_menu, menu);
        UserVM userVM = users().get(uid);
        if (userVM.isContact().get()) {
            menu.findItem(R.id.remove).setVisible(true);
            menu.findItem(R.id.add).setVisible(false);
        } else {
            menu.findItem(R.id.remove).setVisible(false);
            menu.findItem(R.id.add).setVisible(true);
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        } else if (item.getItemId() == R.id.add) {
//            ask(ContactsActor.contactsList().addContact(uid), getString(R.string.profile_adding), new UiAskCallback<Boolean>() {
//                @Override
//                public void onPreStart() {
//
//                }
//
//                @Override
//                public void onCompleted(Boolean res) {
//
//                }
//
//                @Override
//                public void onError(Throwable t) {
//
//                }
//            });
            return true;
        } else if (item.getItemId() == R.id.remove) {
//            ask(ContactsActor.contactsList().removeContact(uid), getString(R.string.profile_removing), new UiAskCallback<Boolean>() {
//                @Override
//                public void onPreStart() {
//
//                }
//
//                @Override
//                public void onCompleted(Boolean res) {
//
//                }
//
//                @Override
//                public void onError(Throwable t) {
//
//                }
//            });
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
