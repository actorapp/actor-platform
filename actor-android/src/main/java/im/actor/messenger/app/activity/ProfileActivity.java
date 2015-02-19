package im.actor.messenger.app.activity;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.view.Menu;
import android.view.MenuItem;

import im.actor.messenger.R;
import im.actor.messenger.app.base.BaseBarFragmentActivity;
import im.actor.messenger.app.fragment.profile.ProfileFragment;
import im.actor.messenger.app.intents.Intents;

/**
 * Created by ex3ndr on 12.09.14.
 */
public class ProfileActivity extends BaseBarFragmentActivity {

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

//        getBinder().bind(users().get(uid).getContactModel(), new Listener<Boolean>() {
//            @Override
//            public void onUpdated(Boolean aBoolean) {
//                invalidateOptionsMenu();
//            }
//        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.profile_menu, menu);

        TelephonyManager tm = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        menu.findItem(R.id.call).setVisible(tm.getPhoneType() != TelephonyManager.PHONE_TYPE_NONE);
//        UserModel userModel = users().get(uid);
//        menu.findItem(R.id.remove).setVisible(userModel.getContactModel().getValue());
//        menu.findItem(R.id.add).setVisible(!userModel.getContactModel().getValue());
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        } else if (item.getItemId() == R.id.call) {
            // UserModel userModel = users().get(uid);
            // startActivity(new Intent(Intent.ACTION_DIAL).setData(Uri.parse("tel:+" + userModel.getPhone())));
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
}
