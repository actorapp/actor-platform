/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.core.viewmodel;

import im.actor.core.modules.ModuleContext;
import im.actor.runtime.mvvm.ValueModel;

/**
 * Application initialization View Model
 */
public class AppStateVM {
    private ModuleContext context;
    private ValueModel<Boolean> isAppVisible;
    private ValueModel<Boolean> isDialogsEmpty;
    private ValueModel<Boolean> isContactsEmpty;
    private ValueModel<Boolean> isAppEmpty;
    private ValueModel<Boolean> isAppLoaded;
    private ValueModel<Boolean> isConnecting;
    private ValueModel<Boolean> isSyncing;
    private ValueModel<Integer> globalCounter;
    private ValueModel<Integer> globalTempCounter;

    private boolean isBookImported;
    private boolean isDialogsLoaded;
    private boolean isContactsLoaded;

    /**
     * Constructor of View Model
     *
     * @param context Messenger im.actor.android.modules
     */
    public AppStateVM(ModuleContext context) {
        this.context = context;
        this.isDialogsEmpty = new ValueModel<Boolean>("app.dialogs.empty", context.getPreferences().getBool("app.dialogs.empty", true));
        this.isContactsEmpty = new ValueModel<Boolean>("app.contacts.empty", context.getPreferences().getBool("app.contacts.empty", true));
        this.isAppEmpty = new ValueModel<Boolean>("app.empty", context.getPreferences().getBool("app.empty", true));
        this.globalCounter = new ValueModel<Integer>("app.counter", context.getPreferences().getInt("app.counter", 0));
        this.globalTempCounter = new ValueModel<Integer>("app.temp_counter", 0);
        this.isConnecting = new ValueModel<Boolean>("app.connecting", false);
        this.isSyncing = new ValueModel<Boolean>("app.syncing", false);
        this.isAppVisible = new ValueModel<Boolean>("app.visible", false);

        this.isBookImported = context.getPreferences().getBool("app.contacts.imported", false);
        this.isDialogsLoaded = context.getPreferences().getBool("app.dialogs.loaded", false);
        this.isContactsLoaded = context.getPreferences().getBool("app.contacts.loaded", false);

        this.isAppLoaded = new ValueModel<Boolean>("app.loaded", isBookImported && isDialogsLoaded && isContactsLoaded);
    }

    private void updateLoaded() {
        boolean val = isBookImported && isDialogsLoaded && isContactsLoaded;
        if (isAppLoaded.get() != val) {
            this.isAppLoaded.change(val);
        }
    }

    /**
     * Notify from Modules about global counters changed
     *
     * @param value current value of global counter
     */
    public synchronized void onGlobalCounterChanged(int value) {
        globalCounter.change(value);
        context.getPreferences().putInt("app.counter", value);
        if (!isAppVisible.get()) {
            globalTempCounter.change(value);
        }
    }

    /**
     * Notify when app become visible
     */
    public synchronized void onAppVisible() {
        isAppVisible.change(true);
        globalTempCounter.change(0);
    }

    /**
     * Notify when app become hidden
     */
    public synchronized void onAppHidden() {
        isAppVisible.change(false);
    }

    /**
     * Notify from Modules about dialogs state changed
     *
     * @param isEmpty is dialogs empty
     */
    public synchronized void onDialogsChanged(boolean isEmpty) {
        if (isDialogsEmpty.get() != isEmpty) {
            context.getPreferences().putBool("app.dialogs.empty", isEmpty);
            isDialogsEmpty.change(isEmpty);
        }
        if (!isEmpty) {
            if (isAppEmpty.get()) {
                context.getPreferences().putBool("app.empty", false);
                isAppEmpty.change(false);
            }
        }
    }

    /**
     * Notify from Modules about contacts state changed
     *
     * @param isEmpty is contacts empty
     */
    public synchronized void onContactsChanged(boolean isEmpty) {
        if (isContactsEmpty.get() != isEmpty) {
            context.getPreferences().putBool("app.contacts.empty", isEmpty);
            isContactsEmpty.change(isEmpty);
        }
        if (!isEmpty) {
            if (isAppEmpty.get()) {
                context.getPreferences().putBool("app.empty", false);
                isAppEmpty.change(false);
            }
        }
    }

    /**
     * Notify from Modules about phone import completed
     */
    public synchronized void onPhoneImported() {
        if (!isBookImported) {
            isBookImported = true;
            context.getPreferences().putBool("app.contacts.imported", true);
            updateLoaded();
        }
    }

    /**
     * Notify from Modules about dialog load completed
     */
    public synchronized void onDialogsLoaded() {
        if (!isDialogsLoaded) {
            isDialogsLoaded = true;
            context.getPreferences().putBool("app.dialogs.loaded", true);
            updateLoaded();
        }
    }

    /**
     * Notify from Modules about contacts load completed
     */
    public synchronized void onContactsLoaded() {
        if (!isContactsLoaded) {
            isContactsLoaded = true;
            context.getPreferences().putBool("app.contacts.loaded", true);
            updateLoaded();
        }
    }

    /**
     * Dialogs empty View Model
     *
     * @return Value Model of Boolean
     */
    public ValueModel<Boolean> getIsDialogsEmpty() {
        return isDialogsEmpty;
    }

    /**
     * Contacts empty View Model
     *
     * @return Value Model of Boolean
     */
    public ValueModel<Boolean> getIsContactsEmpty() {
        return isContactsEmpty;
    }

    /**
     * App loaded View Model
     *
     * @return Value Model of Boolean
     */
    public ValueModel<Boolean> getIsAppLoaded() {
        return isAppLoaded;
    }

    /**
     * App empty View Model
     *
     * @return View Model of Boolean
     */
    public ValueModel<Boolean> getIsAppEmpty() {
        return isAppEmpty;
    }

    /**
     * Is syncing in progress
     *
     * @return View Model of Boolean
     */
    public ValueModel<Boolean> getIsSyncing() {
        return isSyncing;
    }

    /**
     * Is Connecting in progress
     *
     * @return View Model of Boolean
     */
    public ValueModel<Boolean> getIsConnecting() {
        return isConnecting;
    }


    /**
     * Gettting global unread counter
     *
     * @return View Model of Integer
     */
    public ValueModel<Integer> getGlobalCounter() {
        return globalCounter;
    }

    /**
     * Getting global unread counter that hiddes when app is opened
     *
     * @return View Model of Integer
     */
    public ValueModel<Integer> getGlobalTempCounter() {
        return globalTempCounter;
    }

    /**
     * Is App visible state
     *
     * @return View Model of Boolean
     */
    public ValueModel<Boolean> getIsAppVisible() {
        return isAppVisible;
    }
}
