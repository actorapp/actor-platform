package im.actor.messenger.app.activity;

import android.content.Intent;
import android.os.Bundle;

import im.actor.messenger.BuildConfig;
import im.actor.messenger.R;
import im.actor.messenger.app.activity.base.ControllerActivity;
import im.actor.messenger.app.activity.controllers.MainBaseController;
import im.actor.messenger.app.activity.controllers.MainPhoneController;
import im.actor.messenger.app.tour.TourActivity;
import im.actor.messenger.core.Core;

import im.actor.model.State;
import im.actor.model.entity.Dialog;
import net.hockeyapp.android.UpdateManager;

public class MainActivity extends ControllerActivity<MainBaseController> {

    @Override
    public MainBaseController onCreateController() {
        return new MainPhoneController(this);
    }

    @Override
    public void onCreate(Bundle savedInstance) {
        super.onCreate(savedInstance);

        checkForUpdates();

        if (Core.messenger().getAuth().getState() != State.LOGGED_IN) {
            startActivity(new Intent(this, TourActivity.class));
            finish();
            return;
        }

        getWindow().setBackgroundDrawable(null);
    }

    public void onDialogClicked(Dialog item) {
        getController().onItemClicked(item);
    }

    private void checkForUpdates() {
        if (BuildConfig.ENABLE_HOCKEY) {
            UpdateManager.register(this, getString(R.string.hockey_app_id));
        }
    }
}
