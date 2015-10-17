/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.core.viewmodel;

import com.google.j2objc.annotations.Property;

import im.actor.core.modules.ModuleContext;
import im.actor.core.modules.events.AppVisibleChanged;
import im.actor.core.modules.events.ConnectingStateChanged;
import im.actor.core.viewmodel.generics.BooleanValueModel;
import im.actor.core.viewmodel.generics.IntValueModel;
import im.actor.runtime.eventbus.BusSubscriber;
import im.actor.runtime.eventbus.Event;
import im.actor.runtime.mvvm.ValueModel;

/**
 * Application initialization View Model
 */
public class AppStateVM {
    @Property("nonatomic, readonly")
    private ModuleContext context;
    @Property("nonatomic, readonly")
    private BooleanValueModel isAppVisible;
    @Property("nonatomic, readonly")
    private BooleanValueModel isDialogsEmpty;
    @Property("nonatomic, readonly")
    private BooleanValueModel isContactsEmpty;
    @Property("nonatomic, readonly")
    private BooleanValueModel isAppEmpty;
    @Property("nonatomic, readonly")
    private BooleanValueModel isAppLoaded;
    @Property("nonatomic, readonly")
    private BooleanValueModel isConnecting;
    @Property("nonatomic, readonly")
    private BooleanValueModel isSyncing;
    @Property("nonatomic, readonly")
    private IntValueModel globalCounter;
    @Property("nonatomic, readonly")
    private IntValueModel globalTempCounter;

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
        this.isDialogsEmpty = new BooleanValueModel("app.dialogs.empty", context.getPreferences().getBool("app.dialogs.empty", true));
        this.isContactsEmpty = new BooleanValueModel("app.contacts.empty", context.getPreferences().getBool("app.contacts.empty", true));
        this.isAppEmpty = new BooleanValueModel("app.empty", context.getPreferences().getBool("app.empty", true));
        this.globalCounter = new IntValueModel("app.counter", context.getPreferences().getInt("app.counter", 0));
        this.globalTempCounter = new IntValueModel("app.temp_counter", 0);
        this.isConnecting = new BooleanValueModel("app.connecting", false);
        this.isSyncing = new BooleanValueModel("app.syncing", false);
        this.isAppVisible = new BooleanValueModel("app.visible", false);

        this.isBookImported = context.getPreferences().getBool("app.contacts.imported", false);
        this.isDialogsLoaded = context.getPreferences().getBool("app.dialogs.loaded", false);
        this.isContactsLoaded = context.getPreferences().getBool("app.contacts.loaded", false);

        this.isAppLoaded = new BooleanValueModel("app.loaded", isBookImported && isDialogsLoaded && isContactsLoaded);

        context.getEvents().subscribe(new BusSubscriber() {
            @Override
            public void onBusEvent(Event event) {
                if (event instanceof AppVisibleChanged) {
                    if (((AppVisibleChanged) event).isVisible()) {
                        isAppVisible.change(true);
                        globalTempCounter.change(0);
                    } else {
                        isAppVisible.change(false);
                    }
                }
            }
        }, AppVisibleChanged.EVENT);

        context.getEvents().subscribe(new BusSubscriber() {
            @Override
            public void onBusEvent(Event event) {
                isConnecting.change(((ConnectingStateChanged) event).isConnecting());
            }
        }, ConnectingStateChanged.EVENT);
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
    public BooleanValueModel getIsDialogsEmpty() {
        return isDialogsEmpty;
    }

    /**
     * Contacts empty View Model
     *
     * @return Value Model of Boolean
     */
    public BooleanValueModel getIsContactsEmpty() {
        return isContactsEmpty;
    }

    /**
     * App loaded View Model
     *
     * @return Value Model of Boolean
     */
    public BooleanValueModel getIsAppLoaded() {
        return isAppLoaded;
    }

    /**
     * App empty View Model
     *
     * @return View Model of Boolean
     */
    public BooleanValueModel getIsAppEmpty() {
        return isAppEmpty;
    }

    /**
     * Is syncing in progress
     *
     * @return View Model of Boolean
     */
    public BooleanValueModel getIsSyncing() {
        return isSyncing;
    }

    /**
     * Is Connecting in progress
     *
     * @return View Model of Boolean
     */
    public BooleanValueModel getIsConnecting() {
        return isConnecting;
    }


    /**
     * Gettting global unread counter
     *
     * @return View Model of Integer
     */
    public IntValueModel getGlobalCounter() {
        return globalCounter;
    }

    /**
     * Getting global unread counter that hiddes when app is opened
     *
     * @return View Model of Integer
     */
    public IntValueModel getGlobalTempCounter() {
        return globalTempCounter;
    }

    /**
     * Is App visible state
     *
     * @return View Model of Boolean
     */
    public BooleanValueModel getIsAppVisible() {
        return isAppVisible;
    }
}
