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
    private ValueModel<Boolean> isAppEmpty;
    private ValueModel<Boolean> isAppLoaded;

    private boolean isBookImported;
    private boolean isDialogsLoaded;
    private boolean isContactsLoaded;

    public AppStateVM(Modules modules) {
        this.modules = modules;
        this.isDialogsEmpty = new ValueModel<Boolean>("app.dialogs.empty", modules.getPreferences().getBool("app.dialogs.empty", true));
        this.isContactsEmpty = new ValueModel<Boolean>("app.contacts.empty", modules.getPreferences().getBool("app.contacts.empty", true));
        this.isAppEmpty = new ValueModel<Boolean>("app.empty", modules.getPreferences().getBool("app.empty", true));

        this.isBookImported = modules.getPreferences().getBool("app.contacts.imported", false);
        this.isDialogsLoaded = modules.getPreferences().getBool("app.dialogs.loaded", false);
        this.isContactsLoaded = modules.getPreferences().getBool("app.contacts.loaded", false);

        this.isAppLoaded = new ValueModel<Boolean>("app.loaded", isBookImported && isDialogsLoaded && isContactsLoaded);

        Log.d("AppStateVM", "init: " + isBookImported);
    }

    private void updateLoaded() {
        Log.d("AppStateVM", "updateLoaded");
        boolean val = isBookImported && isDialogsLoaded && isContactsLoaded;
        if (isAppLoaded.get() != val) {
            Log.d("AppStateVM", "updateLoaded:apply");
            this.isAppLoaded.change(val);
        }
    }

    public synchronized void onDialogsChanged(boolean isEmpty) {
        if (isDialogsEmpty.get() != isEmpty) {
            modules.getPreferences().putBool("app.dialogs.empty", isEmpty);
            isDialogsEmpty.change(isEmpty);
        }
        if (!isEmpty) {
            if (isAppEmpty.get()) {
                modules.getPreferences().putBool("app.empty", false);
                isAppEmpty.change(false);
            }
        }
    }

    public synchronized void onContactsChanged(boolean isEmpty) {
        if (isContactsEmpty.get() != isEmpty) {
            modules.getPreferences().putBool("app.contacts.empty", isEmpty);
            isContactsEmpty.change(isEmpty);
        }
        if (!isEmpty) {
            if (isAppEmpty.get()) {
                modules.getPreferences().putBool("app.empty", false);
                isAppEmpty.change(false);
            }
        }
    }

    public synchronized void onPhoneImported() {
        Log.d("AppStateVM", "onPhoneImported");
        if (!isBookImported) {
            Log.d("AppStateVM", "onPhoneImported:apply");
            isBookImported = true;
            modules.getPreferences().putBool("app.contacts.imported", true);
            updateLoaded();
        }
    }

    public synchronized void onDialogsLoaded() {
        Log.d("AppStateVM", "onDialogsLoaded");
        if (!isDialogsLoaded) {
            Log.d("AppStateVM", "onDialogsLoaded:apply");
            isDialogsLoaded = true;
            modules.getPreferences().putBool("app.dialogs.loaded", true);
            updateLoaded();
        }
    }

    public synchronized void onContactsLoaded() {
        Log.d("AppStateVM", "onContactsLoaded");
        if (!isContactsLoaded) {
            Log.d("AppStateVM", "onContactsLoaded:apply");
            isContactsLoaded = true;
            modules.getPreferences().putBool("app.contacts.loaded", true);
            updateLoaded();
        }
    }


    public ValueModel<Boolean> getIsDialogsEmpty() {
        return isDialogsEmpty;
    }

    public ValueModel<Boolean> getIsContactsEmpty() {
        return isContactsEmpty;
    }

    public ValueModel<Boolean> getIsAppLoaded() {
        return isAppLoaded;
    }

    public ValueModel<Boolean> getIsAppEmpty() {
        return isAppEmpty;
    }
}
