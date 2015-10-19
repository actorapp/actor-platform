package im.actor.messenger.app.activity.controllers;

import im.actor.messenger.app.activity.ActorMainActivity;
import im.actor.messenger.app.activity.base.Controller;
import im.actor.core.entity.Dialog;

/**
 * Created by ex3ndr on 25.10.14.
 */
public abstract class MainBaseController extends Controller<ActorMainActivity> {
    protected MainBaseController(ActorMainActivity activity) {
        super(activity);
    }

    public abstract void onItemClicked(Dialog dialogItem);
}
