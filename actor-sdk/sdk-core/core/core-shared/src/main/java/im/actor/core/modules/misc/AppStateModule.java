/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.core.modules.misc;

import im.actor.core.modules.AbsModule;
import im.actor.core.modules.ModuleContext;
import im.actor.core.viewmodel.AppStateVM;
import im.actor.core.viewmodel.GlobalStateVM;

public class AppStateModule extends AbsModule {

    private AppStateVM appStateVM;
    private GlobalStateVM globalStateVM;

    public AppStateModule(ModuleContext context) {
        super(context);

        globalStateVM = new GlobalStateVM(context);
    }

    public void run() {
        this.appStateVM = new AppStateVM(context());
    }

    public void onDialogsUpdate(boolean isEmpty) {
        appStateVM.onDialogsChanged(isEmpty);
    }

    public void onContactsUpdate(boolean isEmpty) {
        appStateVM.onContactsChanged(isEmpty);
    }

    public void onBookImported() {
        appStateVM.onPhoneImported();
    }

    public void onContactsLoaded() {
        appStateVM.onContactsLoaded();
    }

    public void onDialogsLoaded() {
        appStateVM.onDialogsLoaded();
    }

    public AppStateVM getAppStateVM() {
        return appStateVM;
    }

    public GlobalStateVM getGlobalStateVM() {
        return globalStateVM;
    }

    public void resetModule() {
        // TODO: Implement
    }
}
