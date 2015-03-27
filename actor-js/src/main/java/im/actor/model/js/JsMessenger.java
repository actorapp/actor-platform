package im.actor.model.js;

import im.actor.model.Configuration;
import im.actor.model.Messenger;
import im.actor.model.entity.Dialog;
import im.actor.model.js.angular.AngularList;
import im.actor.model.js.angular.AngularModule;
import im.actor.model.js.angular.AngularValue;
import im.actor.model.js.entity.JsDialog;
import im.actor.model.js.entity.JsUser;

/**
 * Created by ex3ndr on 27.03.15.
 */
public class JsMessenger extends Messenger {

    private AngularModule angularModule;

    public JsMessenger(Configuration configuration) {
        super(configuration);
        this.angularModule = new AngularModule(modules);
    }

    public AngularList<JsDialog, Dialog> getDialogsList() {
        return angularModule.getDialogsList();
    }

    public AngularValue<JsUser> getUser(int uid) {
        return angularModule.getUser(uid);
    }
}