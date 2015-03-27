package im.actor.model.js.angular;

import im.actor.model.entity.Dialog;
import im.actor.model.js.entity.JsDialog;
import im.actor.model.js.providers.storage.JsListEngine;
import im.actor.model.modules.BaseModule;
import im.actor.model.modules.Modules;

/**
 * Created by ex3ndr on 27.03.15.
 */
public class AngularModule extends BaseModule {
    private AngularList<JsDialog, Dialog> dialogsList;

    public AngularModule(Modules modules) {
        super(modules);
    }

    public AngularList<JsDialog, Dialog> getDialogsList() {
        if (dialogsList == null) {
            dialogsList = new AngularList<JsDialog, Dialog>((JsListEngine<Dialog>) modules().getMessagesModule().getDialogsEngine(),
                    JsDialog.CONVERTER, modules().getI18nEngine());
        }
        return dialogsList;
    }
}
