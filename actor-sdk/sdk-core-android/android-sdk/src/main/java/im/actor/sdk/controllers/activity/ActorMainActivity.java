package im.actor.sdk.controllers.activity;

import android.os.Bundle;


import im.actor.core.entity.Contact;
import im.actor.sdk.ActorSDK;
import im.actor.sdk.controllers.activity.base.ControllerActivity;
import im.actor.sdk.controllers.activity.controllers.MainBaseController;
import im.actor.sdk.controllers.activity.controllers.MainPhoneController;
import im.actor.core.entity.Dialog;

public class ActorMainActivity extends ControllerActivity<MainBaseController> {

    @Override
    public MainBaseController onCreateController() {
        MainPhoneController mainPhoneController = ActorSDK.sharedActor().getDelegate().getMainPhoneController(this);
        return mainPhoneController != null ? mainPhoneController : new MainPhoneController(this);
    }

    @Override
    public void onCreate(Bundle savedInstance) {
        super.onCreate(savedInstance);


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
        getController().onDialogClicked(item);
    }


    public void onContactClicked(Contact contact) {
        getController().onContactClicked(contact);
    }
}
