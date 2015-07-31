package im.actor.messenger.app.activity.base;

import android.content.res.Configuration;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import im.actor.messenger.app.activity.BaseActivity;

/**
 * Created by ex3ndr on 25.10.14.
 */
public abstract class ControllerActivity<T extends Controller> extends BaseActivity {
    private T controller;

    public abstract T onCreateController();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (controller == null) {
            controller = onCreateController();
        }
        controller.onCreate(savedInstanceState);
    }

    public T getController() {
        return controller;
    }

    @Override
    protected void onPause() {
        super.onPause();
        controller.onPause();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return controller.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return controller.onOptionsItemSelected(item);
    }


    @Override
    public void onBackPressed() {
        if (!controller.onBackPressed()) {
            super.onBackPressed();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        controller.onResume();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        controller.onConfigurationChanged(newConfig);
    }
}

