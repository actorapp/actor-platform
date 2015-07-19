package im.actor.messenger.app.fragment.group;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.widget.FrameLayout;

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
        FrameLayout contentFrame = (FrameLayout) findViewById(R.id.content_frame);
        contentFrame.setBackgroundColor(getResources().getColor(android.R.color.transparent));
        getWindow().setBackgroundDrawable(new ColorDrawable(0));
        Fragment fr = new JoinPublicGroupPopUpFragment();
        Bundle b = new Bundle();
        b.putByteArray("avatar", getIntent().getByteArrayExtra("avatar"));
        b.putInt("id", getIntent().getIntExtra("id", 0));
        b.putString("title", getIntent().getStringExtra("title"));
        b.putString("description", getIntent().getStringExtra("description"));
        b.putInt("members", getIntent().getIntExtra("members", 0));
        b.putLong("accessHash", getIntent().getLongExtra("accessHash", 0));
        b.putBoolean("isMember", getIntent().getBooleanExtra("isMember", false));
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
