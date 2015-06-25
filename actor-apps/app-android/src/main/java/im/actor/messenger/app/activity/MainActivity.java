package im.actor.messenger.app.activity;

import android.content.Intent;
import android.os.Bundle;

import net.hockeyapp.android.UpdateManager;

import im.actor.messenger.app.Core;
import im.actor.messenger.app.activity.base.ControllerActivity;
import im.actor.messenger.app.activity.controllers.MainBaseController;
import im.actor.messenger.app.activity.controllers.MainPhoneController;
import im.actor.messenger.app.fragment.tour.TourActivity;
import im.actor.model.AuthState;
import im.actor.model.entity.Dialog;

import static im.actor.messenger.app.Core.messenger;

public class MainActivity extends ControllerActivity<MainBaseController> {

    @Override
    public MainBaseController onCreateController() {
        return new MainPhoneController(this);
    }

    @Override
    public void onCreate(Bundle savedInstance) {

        super.onCreate(savedInstance);


        checkForUpdates();

        if (messenger().getAuthState() != AuthState.LOGGED_IN) {
            startActivity(new Intent(this, TourActivity.class));
            finish();
            return;
        }

        // getWindow().setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.bg_main)));
        getWindow().setBackgroundDrawable(null);
    }

    @Override
    protected void onResume() {
        super.onResume();
        messenger().trackMainScreensOpen();
    }

    @Override
    protected void onPause() {
        super.onPause();
        messenger().trackMainScreensClosed();
    }

    public void onDialogClicked(Dialog item) {
        getController().onItemClicked(item);
    }

    private void checkForUpdates() {
        if (Core.core().getHockeyToken() != null) {
            UpdateManager.register(this, Core.core().getHockeyToken());
        }
    }
}
