package im.actor.sdk.controllers.activity;

import android.content.Intent;
import android.os.Bundle;


import im.actor.messenger.app.core.Core;
import im.actor.sdk.controllers.activity.base.ControllerActivity;
import im.actor.sdk.controllers.activity.controllers.MainBaseController;
import im.actor.sdk.controllers.activity.controllers.MainPhoneController;
import im.actor.sdk.controllers.fragment.tour.TourActivity;
import im.actor.core.AuthState;
import im.actor.core.entity.Dialog;

import static im.actor.messenger.app.core.Core.messenger;

public class MainActivity extends ControllerActivity<MainBaseController> {

    @Override
    public MainBaseController onCreateController() {
        return new MainPhoneController(this);
    }

    @Override
    public void onCreate(Bundle savedInstance) {

        super.onCreate(savedInstance);


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
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    public void onDialogClicked(Dialog item) {
        getController().onItemClicked(item);
    }


}
