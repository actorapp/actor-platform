package im.actor.sdk.controllers.activity.controllers;

import im.actor.core.entity.Contact;
import im.actor.sdk.controllers.activity.ActorMainActivity;
import im.actor.sdk.controllers.activity.base.Controller;
import im.actor.core.entity.Dialog;

/**
 * Created by ex3ndr on 25.10.14.
 */
public abstract class MainBaseController extends Controller<ActorMainActivity> {
    protected MainBaseController(ActorMainActivity activity) {
        super(activity);
    }

    public abstract void onDialogClicked(Dialog dialogItem);

    public abstract void onContactClicked(Contact contact);
}
