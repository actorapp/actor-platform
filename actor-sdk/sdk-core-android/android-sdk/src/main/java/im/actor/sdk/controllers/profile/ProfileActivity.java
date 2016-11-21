package im.actor.sdk.controllers.profile;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.widget.Toast;

import im.actor.sdk.ActorSDK;
import im.actor.sdk.R;
import im.actor.sdk.controllers.Intents;
import im.actor.sdk.controllers.activity.BaseFragmentActivity;

import static im.actor.sdk.util.ActorSDKMessenger.users;

public class ProfileActivity extends BaseFragmentActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Loading explicit UID
        int uid = getIntent().getIntExtra(Intents.EXTRA_UID, 0);
        // Trying to load UID from URL
        if (uid == 0) {
            try {
                uid = Integer.parseInt(getIntent().getData().getPath().replace(")", "").split("/")[2]);
            } catch (Exception e) {
                Toast.makeText(this, getString(R.string.profile_cant_find_user), Toast.LENGTH_SHORT).show();
                finish();
                return;
            }
        }
        // Validation of UID
        try {
            users().get(uid);
        } catch (Exception e) {
            Toast.makeText(this, getString(R.string.profile_cant_find_user), Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // If Activity is not restored - create new fragment
        if (savedInstanceState == null) {
            Fragment fragment = ActorSDK.sharedActor().getDelegate().fragmentForProfile(uid);
            if (fragment == null) {
                fragment = ProfileFragment.create(uid);
            }
            showFragment(fragment, false);
        }
    }
}