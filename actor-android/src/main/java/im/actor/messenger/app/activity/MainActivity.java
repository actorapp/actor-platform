package im.actor.messenger.app.activity;

import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;

import im.actor.messenger.BuildConfig;
import im.actor.messenger.R;
import im.actor.messenger.app.activity.base.ControllerActivity;
import im.actor.messenger.app.activity.controllers.MainBaseController;
import im.actor.messenger.app.activity.controllers.MainPhoneController;
import im.actor.messenger.app.tour.TourActivity;
import im.actor.messenger.storage.scheme.messages.DialogItem;

import net.hockeyapp.android.UpdateManager;

import static im.actor.messenger.core.Core.auth;

public class MainActivity extends ControllerActivity<MainBaseController> {

    @Override
    public MainBaseController onCreateController() {
        return new MainPhoneController(this);
    }

    @Override
    public void onCreate(Bundle savedInstance) {
        super.onCreate(savedInstance);

        checkForUpdates();

        if (!auth().isAuthorized()) {
            // startActivity(new Intent(this, AuthActivity.class));
            startActivity(new Intent(this, TourActivity.class));
            finish();
            return;
        }

        getWindow().setBackgroundDrawable(null);
    }

    public void onDialogClicked(DialogItem item) {
        getController().onItemClicked(item);
    }

    private void checkForUpdates() {
        if (BuildConfig.ENABLE_HOCKEY) {
            UpdateManager.register(this, getString(R.string.hockey_app_id));
        }
    }
}
