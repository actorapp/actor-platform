package im.actor.sdk.controllers.root;

import im.actor.core.entity.Contact;
import im.actor.sdk.controllers.activity.base.Controller;
import im.actor.core.entity.Dialog;

public abstract class MainBaseController extends Controller<RootActivity> {

    protected MainBaseController(RootActivity activity) {
        super(activity);
    }

    public abstract void onDialogClicked(Dialog dialogItem);

    public abstract void onContactClicked(Contact contact);
}
