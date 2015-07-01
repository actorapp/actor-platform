package im.actor.messenger.app.fragment.group;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;

import im.actor.messenger.R;
import im.actor.messenger.app.Intents;
import im.actor.messenger.app.base.BaseFragmentActivity;

/**
 * Created by korka on 30.06.15.
 */
public class JoinGroupPopUpActivity extends FragmentActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_base_fragment);
        int chatId = getIntent().getIntExtra(Intents.EXTRA_GROUP_ID, 0);

//        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
//        getSupportActionBar().setTitle(null);

        Fragment fr = new JoinPublicGroupPopUpFragment();
        Bundle b = new Bundle();
        b.putByteArray("group", getIntent().getByteArrayExtra("group"));
        fr.setArguments(b);

        if (savedInstanceState == null) {
            showFragment(fr, false, false);
        }
    }

    public void showFragment(final Fragment fragment, final boolean addToBackStack, final boolean isAnimated) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.content_frame, fragment);
        if (addToBackStack) {
            transaction.addToBackStack(null);
        }
        transaction.commit();
    }
}
