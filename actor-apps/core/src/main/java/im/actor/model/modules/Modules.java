/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.model.modules;

import im.actor.model.Configuration;
import im.actor.model.Messenger;
import im.actor.model.droidkit.engine.PreferencesStorage;
import im.actor.model.i18n.I18nEngine;
import im.actor.model.modules.utils.PreferenceApiStorage;
import im.actor.model.mvvm.MVVMEngine;
import im.actor.model.network.ActorApi;
import im.actor.model.network.ActorApiCallback;
import im.actor.model.network.Endpoints;
import im.actor.model.util.Timing;

public class Modules {
    private final Configuration configuration;
    private final I18nEngine i18nEngine;
    private final Analytics analytics;
    private final ActorApi actorApi;
    private final Auth auth;
    private final AppStateModule appStateModule;
    private final Messenger messenger;
    private final External external;

    private boolean isAppVisible;
    private volatile PreferencesStorage preferences;
    private volatile Users users;
    private volatile Groups groups;
    private volatile Updates updates;
    private volatile Messages messages;
    private volatile Pushes pushes;
    private volatile Presence presence;
    private volatile Typing typing;
    private volatile Files filesModule;
    private volatile Contacts contacts;
    private volatile Notifications notifications;
    private volatile Settings settings;
    private volatile Profile profile;
    private volatile SearchModule search;
    private volatile Security security;

    public Modules(Messenger messenger, Configuration configuration) {
        this.messenger = messenger;
        this.configuration = configuration;

        Timing timing = new Timing("MODULES_INIT");

        timing.section("I18N");
        this.i18nEngine = new I18nEngine(configuration.getLocaleProvider(), this);

        timing.section("Preferences");
        this.preferences = configuration.getStorageProvider().createPreferencesStorage();

        timing.section("Analytics");
        this.analytics = new Analytics(this);

        timing.section("API");
        this.actorApi = new ActorApi(new Endpoints(configuration.getEndpoints()),
                new PreferenceApiStorage(preferences),
                new ActorApiCallbackImpl(),
                configuration.getNetworkProvider(), configuration.isEnableNetworkLogging(),
                configuration.getMinDelay(),
                configuration.getMaxDelay(),
                configuration.getMaxFailureCount());

        timing.section("Auth");
        this.auth = new Auth(this);

        timing.section("Pushes");
        this.pushes = new Pushes(this);

        timing.section("App State");
        this.appStateModule = new AppStateModule(this);

        timing.section("External");
        this.external = new External(this);

        timing.end();
    }

    public void run() {
        this.auth.run();
    }

    public void onLoggedIn() {
        Timing timing = new Timing("ACCOUNT_CREATE");
        timing.section("Users");
        users = new Users(this);
        timing.section("Groups");
        groups = new Groups(this);
        timing.section("Search");
        search = new SearchModule(this);
        timing.section("Security");
        security = new Security(this);
        timing.section("Messages");
        messages = new Messages(this);
        timing.section("Updates");
        updates = new Updates(this);
        timing.section("Presence");
        presence = new Presence(this);
        timing.section("Typing");
        typing = new Typing(this);
        timing.section("Files");
        filesModule = new Files(this);
        timing.section("Notifications");
        notifications = new Notifications(this);
        timing.section("Contacts");
        contacts = new Contacts(this);
        timing.section("Settings");
        settings = new Settings(this);
        timing.section("Profile");
        profile = new Profile(this);
        timing.end();

        timing = new Timing("ACCOUNT_RUN");
        timing.section("Settings");
        settings.run();
        timing.section("Files");
        filesModule.run();
        timing.section("Search");
        search.run();
        timing.section("Notifications");
        notifications.run();
        timing.section("AppState");
        appStateModule.run();
        timing.section("Contacts");
        contacts.run();
        timing.section("Messages");
        messages.run();
        timing.section("Updates");
        updates.run();
        timing.section("Presence");
        presence.run();
        timing.end();

        // Notify about app visible
        if (isAppVisible) {
            presence.onAppVisible();
            notifications.onAppVisible();
        } else {
            // Doesn't notify presence to avoid unessessary setOnline request
            // presence.onAppHidden();
            notifications.onAppHidden();
        }

        messenger.onLoggedIn();
    }

    public void onLoggedOut() {
        MVVMEngine.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                // Reset Storage
                getConfiguration().getStorageProvider().resetStorage();
                // Kill app
                getConfiguration().getLifecycleProvider().killApp();
            }
        });
    }

    public PreferencesStorage getPreferences() {
        return preferences;
    }

    public Configuration getConfiguration() {
        return configuration;
    }

    public Auth getAuthModule() {
        return auth;
    }

    public Users getUsersModule() {
        return users;
    }

    public Groups getGroupsModule() {
        return groups;
    }

    public Messages getMessagesModule() {
        return messages;
    }

    public Updates getUpdatesModule() {
        return updates;
    }

    public Typing getTypingModule() {
        return typing;
    }

    public Presence getPresenceModule() {
        return presence;
    }

    public ActorApi getActorApi() {
        return actorApi;
    }

    public I18nEngine getI18nEngine() {
        return i18nEngine;
    }

    public Contacts getContactsModule() {
        return contacts;
    }

    public Files getFilesModule() {
        return filesModule;
    }

    public Notifications getNotifications() {
        return notifications;
    }

    public Settings getSettings() {
        return settings;
    }

    public Profile getProfile() {
        return profile;
    }

    public AppStateModule getAppStateModule() {
        return appStateModule;
    }

    public Pushes getPushes() {
        return pushes;
    }

    public Security getSecurity() {
        return security;
    }

    public SearchModule getSearch() {
        return search;
    }

    public Messenger getMessenger() {
        return messenger;
    }

    public Analytics getAnalytics() {
        return analytics;
    }

    public External getExternal() {
        return external;
    }

    public void onAppVisible() {
        isAppVisible = true;
        actorApi.forceNetworkCheck();
        analytics.trackAppVisible();
        if (getPresenceModule() != null) {
            getPresenceModule().onAppVisible();
            getNotifications().onAppVisible();
        }
    }

    public void onAppHidden() {
        isAppVisible = false;
        analytics.trackAppHidden();
        if (getPresenceModule() != null) {
            getPresenceModule().onAppHidden();
            getNotifications().onAppHidden();
        }
    }

    private class ActorApiCallbackImpl implements ActorApiCallback {

        @Override
        public void onAuthIdInvalidated() {
            onLoggedOut();
        }

        @Override
        public void onNewSessionCreated() {
            if (updates != null) {
                updates.onNewSessionCreated();
            }
            if (presence != null) {
                presence.onNewSessionCreated();
            }
        }

        @Override
        public void onUpdateReceived(Object obj) {
            if (updates != null) {
                updates.onUpdateReceived(obj);
            }
        }
    }
}