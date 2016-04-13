/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.core.viewmodel;

import com.google.j2objc.annotations.Property;

import im.actor.core.modules.ModuleContext;
import im.actor.core.events.AppVisibleChanged;
import im.actor.core.events.ConnectingStateChanged;
import im.actor.core.viewmodel.generics.BooleanValueModel;
import im.actor.core.viewmodel.generics.IntValueModel;
import im.actor.runtime.eventbus.BusSubscriber;
import im.actor.runtime.eventbus.Event;

/**
 * Application initialization View Model
 */
public class AppStateVM {
    @Property("nonatomic, readonly")
    private ModuleContext context;
    @Property("nonatomic, readonly")
    private BooleanValueModel isDialogsEmpty;
    @Property("nonatomic, readonly")
    private BooleanValueModel isContactsEmpty;
    @Property("nonatomic, readonly")
    private BooleanValueModel isAppEmpty;
    @Property("nonatomic, readonly")
    private BooleanValueModel isAppLoaded;

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

        this.isBookImported = context.getPreferences().getBool("app.contacts.imported", false);
        this.isDialogsLoaded = context.getPreferences().getBool("app.dialogs.loaded", false);
        this.isContactsLoaded = context.getPreferences().getBool("app.contacts.loaded", false);

        this.isAppLoaded = new BooleanValueModel("app.loaded", isBookImported && isDialogsLoaded && isContactsLoaded);
    }

    private void updateLoaded() {
        boolean val = isBookImported && isDialogsLoaded && isContactsLoaded;
        if (isAppLoaded.get() != val) {
            this.isAppLoaded.change(val);
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

}
