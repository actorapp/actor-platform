package im.actor.messenger.app.base;

import android.app.FragmentTransaction;
import android.os.Bundle;
import android.view.MenuItem;

import im.actor.messenger.R;

public class BaseFragmentActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_base_fragment);
    }

    public void showFragment(final BaseFragment fragment, final boolean addToBackStack, final boolean isAnimated) {
        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        transaction.add(R.id.content_frame, fragment);
        if (addToBackStack) {
            transaction.addToBackStack(null);
        }
        transaction.commit();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
        }
        return false;
    }
}
