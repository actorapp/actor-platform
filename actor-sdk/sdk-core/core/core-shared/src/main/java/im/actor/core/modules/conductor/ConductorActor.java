package im.actor.core.modules.conductor;

import java.util.ArrayList;

import im.actor.core.AutoJoinType;
import im.actor.core.api.rpc.RequestNotifyAboutDeviceInfo;
import im.actor.core.api.rpc.ResponseVoid;
import im.actor.core.modules.ModuleActor;
import im.actor.core.modules.ModuleContext;
import im.actor.core.util.JavaUtil;
import im.actor.core.viewmodel.AppStateVM;
import im.actor.runtime.Log;

/**
 * Launching App in stages
 * 1. Download existing Contacts and Dialogs
 * 2. Start Phone Book uploading
 * 3. When app is fully loaded (at least one contact or message in dialogs) then invoke
 * initialization actions such as joining channel
 */
public class ConductorActor extends ModuleActor {

    public static final String TAG = "Conductor";
    private static final ResponseVoid DUMB = null;
    private static final Integer DUMB2 = null;

    private AppStateVM appStateVM;
    private boolean isStarted = false;

    public ConductorActor(ModuleContext context) {
        super(context);
    }

    @Override
    public void preStart() {
        super.preStart();

        updateDeviceInfoIfNeeded();

        appStateVM = context().getConductor().getAppStateVM();
    }

    public void onFinishLaunching() {
        if (isStarted) {
            return;
        }
        isStarted = true;
        unstashAll();
        
        if (appStateVM.isDialogsLoaded() && appStateVM.isContactsLoaded() && appStateVM.isSettingsLoaded()) {
            onInitialDataDownloaded();
        }

        if (appStateVM.getIsAppLoaded().get() || !appStateVM.getIsAppEmpty().get()) {
            onAppReady();
        }
    }

    //
    // Initial Loading
    //

    public void onDialogsLoaded() {
        Log.d(TAG, "Dialogs Loaded");
        if (!appStateVM.isDialogsLoaded()) {
            appStateVM.onDialogsLoaded();
            if (appStateVM.isContactsLoaded() && appStateVM.isSettingsLoaded()) {
                onInitialDataDownloaded();
            }
        }
    }

    public void onContactsLoaded() {
        Log.d(TAG, "Contacts Loaded");
        if (!appStateVM.isContactsLoaded()) {
            appStateVM.onContactsLoaded();
            if (appStateVM.isDialogsLoaded() && appStateVM.isSettingsLoaded()) {
                onInitialDataDownloaded();
            }
        }
    }

    public void onSettingsLoaded() {
        Log.d(TAG, "Settings Loaded");
        if (!appStateVM.isSettingsLoaded()) {
            appStateVM.onSettingsLoaded();
            if (appStateVM.isDialogsLoaded() && appStateVM.isContactsLoaded()) {
                onInitialDataDownloaded();
            }
        }
    }

    /**
     * Called after dialogs, contacts and settings are downloaded from server
     */
    public void onInitialDataDownloaded() {
        Log.d(TAG, "Initial Data Loaded");
        context().getContactsModule().startImport();
        if (appStateVM.isBookImported()) {
            onAppLoaded();
        }
    }

    public void onBookImported() {
        Log.d(TAG, "Book Uploaded");
        if (!appStateVM.isBookImported()) {
            appStateVM.onPhoneImported();
            onAppLoaded();
        }
    }

    //
    // Data Sync
    //

    public void onDialogsChanged(boolean isEmpty) {
        boolean wasEmpty = appStateVM.getIsAppEmpty().get();
        appStateVM.onDialogsChanged(isEmpty);
        if (wasEmpty && !appStateVM.getIsAppEmpty().get()) {
            onAppReady();
        }
    }

    public void onContactsChanged(boolean isEmpty) {
        boolean wasEmpty = appStateVM.getIsAppEmpty().get();
        appStateVM.onContactsChanged(isEmpty);
        if (wasEmpty && !appStateVM.getIsAppEmpty().get()) {
            onAppReady();
        }
    }

    /**
     * Called after dialogs, contacts and settings are downloaded from server and
     * phone book is imported to server
     */
    public void onAppLoaded() {
        Log.d(TAG, "App Loaded");

        // Joining Groups
        if (config().getAutoJoinType() == AutoJoinType.IMMEDIATELY) {
            joinGroups();
        }
    }

    /**
     * Called after at least one message or contact is in user's phone book
     */
    public void onAppReady() {
        Log.d(TAG, "App Ready");

        // Joining Groups
        if (config().getAutoJoinType() == AutoJoinType.AFTER_INIT) {
            joinGroups();
        }
    }

    private void joinGroups() {
        for (String s : config().getAutoJoinGroups()) {
            if (!context().getSettingsModule().getBooleanValue("auto_join." + s, false)) {
                context().getGroupsModule().joinGroupByToken(s).then(r -> {
                    context().getSettingsModule().setBooleanValue("auto_join." + s, true);
                });
            }
        }
    }

    //
    // Tools
    //

    public void updateDeviceInfoIfNeeded() {

        //
        // Loading Information
        //
        ArrayList<String> langs = new ArrayList<>();
        for (String s : context().getConfiguration().getPreferredLanguages()) {
            langs.add(s);
        }
        final String timeZone = context().getConfiguration().getTimeZone();

        //
        // Checking if information changed
        //
        String expectedLangs = "";
        for (String s : langs) {
            if (!"".equals(expectedLangs)) {
                expectedLangs += ",";
            }
            expectedLangs += s.toLowerCase();
        }

        if (expectedLangs.equals(preferences().getString("device_info_langs")) &&
                JavaUtil.equalsE(timeZone, preferences().getString("device_info_timezone"))) {
            // Already sent
            return;
        }

        //
        // Performing Notification
        //
        final String finalExpectedLangs = expectedLangs;
        api(new RequestNotifyAboutDeviceInfo(langs, timeZone)).then(r -> {
            preferences().putString("device_info_langs", finalExpectedLangs);
            preferences().putString("device_info_timezone", timeZone);
        });
    }


    //
    // Messages
    //

    @Override
    public void onReceive(Object message) {
        if (message instanceof DialogsLoaded) {
            if (!isStarted) {
                stash();
                return;
            }
            onDialogsLoaded();
        } else if (message instanceof ContactsLoaded) {
            if (!isStarted) {
                stash();
                return;
            }
            onContactsLoaded();
        } else if (message instanceof SettingsLoaded) {
            if (!isStarted) {
                stash();
                return;
            }
            onSettingsLoaded();
        } else if (message instanceof BookImported) {
            if (!isStarted) {
                stash();
                return;
            }
            onBookImported();
        } else if (message instanceof ContactsChanged) {
            if (!isStarted) {
                stash();
                return;
            }
            onContactsChanged(((ContactsChanged) message).isEmpty());
        } else if (message instanceof DialogsChanged) {
            if (!isStarted) {
                stash();
                return;
            }
            onDialogsChanged(((DialogsChanged) message).isEmpty());
        } else if (message instanceof FinishLaunching) {
            onFinishLaunching();
        } else {
            super.onReceive(message);
        }
    }

    public static class DialogsLoaded {

    }

    public static class ContactsLoaded {

    }

    public static class BookImported {

    }

    public static class SettingsLoaded {

    }

    public static class ContactsChanged {
        private boolean isEmpty;

        public ContactsChanged(boolean isEmpty) {
            this.isEmpty = isEmpty;
        }

        public boolean isEmpty() {
            return isEmpty;
        }
    }

    public static class DialogsChanged {
        private boolean isEmpty;

        public DialogsChanged(boolean isEmpty) {
            this.isEmpty = isEmpty;
        }

        public boolean isEmpty() {
            return isEmpty;
        }
    }

    public static class FinishLaunching {

    }
}
