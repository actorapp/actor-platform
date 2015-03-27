package im.actor.model.js.angular;

import im.actor.model.entity.Dialog;
import im.actor.model.js.entity.JsDialog;
import im.actor.model.js.entity.JsUser;
import im.actor.model.js.providers.storage.JsListEngine;
import im.actor.model.modules.BaseModule;
import im.actor.model.modules.Modules;
import im.actor.model.mvvm.ModelChangedListener;
import im.actor.model.viewmodel.UserVM;

import java.util.HashMap;

/**
 * Created by ex3ndr on 27.03.15.
 */
public class AngularModule extends BaseModule {
    private AngularList<JsDialog, Dialog> dialogsList;
    private HashMap<Integer, AngularValue<JsUser>> users;

    public AngularModule(Modules modules) {
        super(modules);

        users = new HashMap<Integer, AngularValue<JsUser>>();
    }

    public AngularList<JsDialog, Dialog> getDialogsList() {
        if (dialogsList == null) {
            dialogsList = new AngularList<JsDialog, Dialog>((JsListEngine<Dialog>) modules().getMessagesModule().getDialogsEngine(),
                    JsDialog.CONVERTER, modules().getI18nEngine());
        }
        return dialogsList;
    }

    public AngularValue<JsUser> getUser(int uid) {
        if (!users.containsKey(uid)) {
            UserVM userVM = modules().getUsersModule().getUsersCollection().get(uid);
            final AngularValue<JsUser> value = new AngularValue<JsUser>(JsUser.fromUserVM(userVM));
            userVM.subscribe(new ModelChangedListener<UserVM>() {
                @Override
                public void onChanged(UserVM model) {
                    value.changeValue(JsUser.fromUserVM(model));
                }
            });
            users.put(uid, value);
        }
        return users.get(uid);
    }
}
