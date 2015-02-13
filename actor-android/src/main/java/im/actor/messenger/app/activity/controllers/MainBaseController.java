package im.actor.messenger.app.activity.controllers;

import im.actor.messenger.app.activity.MainActivity;
import im.actor.messenger.app.activity.base.Controller;
import im.actor.messenger.storage.scheme.messages.DialogItem;

/**
 * Created by ex3ndr on 25.10.14.
 */
public abstract class MainBaseController extends Controller<MainActivity> {
    protected MainBaseController(MainActivity activity) {
        super(activity);
    }

    public abstract void onItemClicked(DialogItem dialogItem);
}
