package im.actor.sdk.controllers.activity.controllers;

import im.actor.sdk.controllers.activity.MainActivity;
import im.actor.sdk.controllers.activity.base.Controller;
import im.actor.core.entity.Dialog;

/**
 * Created by ex3ndr on 25.10.14.
 */
public abstract class MainBaseController extends Controller<MainActivity> {
    protected MainBaseController(MainActivity activity) {
        super(activity);
    }

    public abstract void onItemClicked(Dialog dialogItem);
}
