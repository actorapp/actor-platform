package im.actor.messenger.app.base;

import android.app.ProgressDialog;
import android.support.v7.app.ActionBarActivity;

import com.droidkit.mvvm.ui.Binder;

import static im.actor.messenger.core.Core.messenger;

/**
 * Created by ex3ndr on 29.12.14.
 */
public class BaseBarActivity extends ActionBarActivity {
    private final Binder BINDER = new Binder();

    public Binder getBinder() {
        return BINDER;
    }

    @Override
    protected void onResume() {
        super.onResume();
        messenger().onAppVisible();
    }

    @Override
    protected void onPause() {
        super.onPause();
        messenger().onAppHidden();
        BINDER.unbindAll();
    }
}
