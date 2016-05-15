package im.actor.sdk.controllers.activity;

import android.os.Bundle;

import im.actor.core.entity.Contact;
import im.actor.sdk.ActorSDK;
import im.actor.sdk.controllers.activity.base.ControllerActivity;
import im.actor.sdk.controllers.root.MainBaseController;
import im.actor.sdk.controllers.root.MainPhoneController;
import im.actor.core.entity.Dialog;

/**
 * Root Activity of Application. Do not move unless home screen buttons will stop working.
 */
public class ActorMainActivity extends ControllerActivity<MainBaseController> {

    @Override
    public MainBaseController onCreateController() {
        MainPhoneController mainPhoneController = ActorSDK.sharedActor().getDelegate().getMainPhoneController(this);
        return mainPhoneController != null ? mainPhoneController : new MainPhoneController(this);
    }

    @Override
    public void onCreate(Bundle savedInstance) {
        super.onCreate(savedInstance);

        // For faster keyboard opening
        getWindow().setBackgroundDrawable(null);
    }

    public void onDialogClicked(Dialog item) {
        getController().onDialogClicked(item);
    }


    public void onContactClicked(Contact contact) {
        getController().onContactClicked(contact);
    }
}
