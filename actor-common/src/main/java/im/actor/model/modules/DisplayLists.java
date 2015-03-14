package im.actor.model.modules;

import im.actor.model.droidkit.engine.ListEngine;
import im.actor.model.droidkit.engine.ListEngineDisplayExt;
import im.actor.model.entity.Dialog;
import im.actor.model.mvvm.BindedDisplayList;
import im.actor.model.mvvm.MVVMEngine;

/**
 * Created by ex3ndr on 14.03.15.
 */
public class DisplayLists extends BaseModule {

    private BindedDisplayList<Dialog> dialogGlobalList;

    public DisplayLists(Modules modules) {
        super(modules);
    }

    public BindedDisplayList<Dialog> getDialogsGlobalList() {
        MVVMEngine.checkMainThread();
        if (dialogGlobalList == null) {
            dialogGlobalList = buildNewDialogsList();
        }
        return dialogGlobalList;
    }

    public BindedDisplayList<Dialog> buildNewDialogsList() {
        ListEngine<Dialog> dialogsEngine = modules().getMessagesModule().getDialogsEngine();
        if (!(dialogsEngine instanceof ListEngineDisplayExt)) {
            throw new RuntimeException("Dialogs ListEngine must implement ListEngineDisplayExt for using global list");
        }

        BindedDisplayList<Dialog> displayList = new BindedDisplayList<Dialog>((ListEngineDisplayExt<Dialog>) dialogsEngine);
        displayList.initTop();
        return displayList;
    }
}
