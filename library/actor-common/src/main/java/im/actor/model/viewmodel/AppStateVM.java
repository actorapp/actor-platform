package im.actor.model.viewmodel;

import im.actor.model.log.Log;
import im.actor.model.modules.Modules;
import im.actor.model.mvvm.ValueModel;

/**
 * Created by ex3ndr on 28.03.15.
 */
public class AppStateVM {
    private Modules modules;
    private ValueModel<Boolean> isDialogsEmpty;
    private ValueModel<Boolean> isContactsEmpty;
    private ValueModel<AppState> appState;

    public AppStateVM(Modules modules) {
        this.modules = modules;
        this.isDialogsEmpty = new ValueModel<Boolean>("app.dialogs.empty", modules.getPreferences().getBool("app.dialogs.empty", true));
        this.isContactsEmpty = new ValueModel<Boolean>("app.contacts.empty", modules.getPreferences().getBool("app.contacts.empty", true));
        this.appState = new ValueModel<AppState>("app.state", AppState.READY);
    }

    public synchronized void onDialogsChanged(boolean isEmpty) {
        Log.d("AppStateVM", "onDialogsChanged:" + isEmpty);
        if (isDialogsEmpty.get() != isEmpty) {
            Log.d("AppStateVM", "onDialogsChanged:" + isEmpty + ": apply");
            modules.getPreferences().putBool("app.dialogs.empty", isEmpty);
            isDialogsEmpty.change(isEmpty);
        }
    }

    public synchronized void onContactsChanged(boolean isEmpty) {
        if (isDialogsEmpty.get() != isEmpty) {
            modules.getPreferences().putBool("app.contacts.empty", isEmpty);
            isDialogsEmpty.change(isEmpty);
        }
    }

    public ValueModel<Boolean> getIsDialogsEmpty() {
        return isDialogsEmpty;
    }

    public ValueModel<Boolean> getIsContactsEmpty() {
        return isContactsEmpty;
    }

    public ValueModel<AppState> getAppState() {
        return appState;
    }
}
