/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.core.modules;

import im.actor.core.Configuration;
import im.actor.core.Messenger;
import im.actor.core.i18n.I18nEngine;
import im.actor.core.modules.internal.AnalyticsModule;
import im.actor.core.modules.internal.AppStateModule;
import im.actor.core.modules.internal.ContactsModule;
import im.actor.core.modules.internal.DisplayLists;
import im.actor.core.modules.internal.ExternalModule;
import im.actor.core.modules.internal.FilesModule;
import im.actor.core.modules.internal.GroupsModule;
import im.actor.core.modules.internal.MentionsModule;
import im.actor.core.modules.internal.MessagesModule;
import im.actor.core.modules.internal.NotificationsModule;
import im.actor.core.modules.internal.PresenceModule;
import im.actor.core.modules.internal.ProfileModule;
import im.actor.core.modules.internal.PushesModule;
import im.actor.core.modules.internal.SearchModule;
import im.actor.core.modules.internal.SecurityModule;
import im.actor.core.modules.internal.SettingsModule;
import im.actor.core.modules.internal.TypingModule;
import im.actor.core.modules.internal.users.UsersModule;
import im.actor.core.modules.utils.PreferenceApiStorage;
import im.actor.core.network.ActorApi;
import im.actor.core.network.ActorApiCallback;
import im.actor.core.network.Endpoints;
import im.actor.core.util.Timing;
import im.actor.runtime.Storage;
import im.actor.runtime.storage.PreferencesStorage;

public class Modules implements ModuleContext {

    private final Configuration configuration;
    private final I18nEngine i18nEngine;
    private final AnalyticsModule analytics;
    private final ActorApi actorApi;
    private final Authentication authentication;
    private final AppStateModule appStateModule;
    private final Messenger messenger;
    private final ExternalModule external;

    private boolean isAppVisible;
    private volatile PreferencesStorage preferences;
    private volatile UsersModule users;
    private volatile GroupsModule groups;
    private volatile Updates updates;
    private volatile MessagesModule messages;
    private volatile PushesModule pushes;
    private volatile PresenceModule presence;
    private volatile TypingModule typing;
    private volatile FilesModule filesModule;
    private volatile ContactsModule contacts;
    private volatile NotificationsModule notifications;
    private volatile SettingsModule settings;
    private volatile ProfileModule profile;
    private volatile SearchModule search;
    private volatile SecurityModule security;
    private volatile DisplayLists displayLists;
    private volatile MentionsModule mentions;

    public Modules(Messenger messenger, Configuration configuration) {
        this.messenger = messenger;
        this.configuration = configuration;

        Timing timing = new Timing("MODULES_INIT");

        timing.section("I18N");
        this.i18nEngine = new I18nEngine(this);

        timing.section("Preferences");
        this.preferences = Storage.createPreferencesStorage();

        timing.section("Analytics");
        this.analytics = new AnalyticsModule(this);

        timing.section("API");
        this.actorApi = new ActorApi(new Endpoints(configuration.getEndpoints()),
                new PreferenceApiStorage(preferences),
                new ActorApiCallbackImpl(),
                configuration.isEnableNetworkLogging(),
                configuration.getMinDelay(),
                configuration.getMaxDelay(),
                configuration.getMaxFailureCount());

        timing.section("App State");
        this.appStateModule = new AppStateModule(this);

        timing.section("External");
        this.external = new ExternalModule(this);

        timing.section("Pushes");
        this.pushes = new PushesModule(this);

        timing.section("Auth");
        this.authentication = new Authentication(this);
        this.authentication.run();
        timing.end();
    }

//    public void run() {
//        this.auth.run();
//    }

    public void onLoggedIn() {
        Timing timing = new Timing("ACCOUNT_CREATE");
        timing.section("Users");
        users = new UsersModule(this);
        timing.section("Groups");
        groups = new GroupsModule(this);
        timing.section("Search");
        search = new SearchModule(this);
        timing.section("Security");
        security = new SecurityModule(this);
        timing.section("Messages");
        messages = new MessagesModule(this);
        timing.section("Updates");
        updates = new Updates(this);
        timing.section("Presence");
        presence = new PresenceModule(this);
        timing.section("Typing");
        typing = new TypingModule(this);
        timing.section("Files");
        filesModule = new FilesModule(this);
        timing.section("Notifications");
        notifications = new NotificationsModule(this);
        timing.section("Contacts");
        contacts = new ContactsModule(this);
        timing.section("Settings");
        settings = new SettingsModule(this);
        timing.section("Profile");
        profile = new ProfileModule(this);
        timing.section("Mentions");
        mentions = new MentionsModule(this);
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
        timing.section("DisplayLists");
        displayLists = new DisplayLists(this);
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
        im.actor.runtime.Runtime.postToMainThread(new Runnable() {
            @Override
            public void run() {
                // Reset Storage
                Storage.resetStorage();
                // Kill app
                im.actor.runtime.Runtime.killApp();
            }
        });
    }

    @Override
    public Module getModule(String name) {
        return null;
    }

    public PreferencesStorage getPreferences() {
        return preferences;
    }

    public Configuration getConfiguration() {
        return configuration;
    }

    public Authentication getAuthModule() {
        return authentication;
    }

    public UsersModule getUsersModule() {
        return users;
    }

    public GroupsModule getGroupsModule() {
        return groups;
    }

    public MessagesModule getMessagesModule() {
        return messages;
    }

    public Updates getUpdatesModule() {
        return updates;
    }

    public TypingModule getTypingModule() {
        return typing;
    }

    public PresenceModule getPresenceModule() {
        return presence;
    }

    public ActorApi getActorApi() {
        return actorApi;
    }

    public I18nEngine getI18nModule() {
        return i18nEngine;
    }

    public ContactsModule getContactsModule() {
        return contacts;
    }

    public FilesModule getFilesModule() {
        return filesModule;
    }

    public NotificationsModule getNotificationsModule() {
        return notifications;
    }

    public SettingsModule getSettingsModule() {
        return settings;
    }

    public ProfileModule getProfileModule() {
        return profile;
    }

    public AppStateModule getAppStateModule() {
        return appStateModule;
    }

    public PushesModule getPushesModule() {
        return pushes;
    }

    public SecurityModule getSecurityModule() {
        return security;
    }

    public SearchModule getSearchModule() {
        return search;
    }

    public Messenger getMessenger() {
        return messenger;
    }

    public AnalyticsModule getAnalyticsModule() {
        return analytics;
    }

    public ExternalModule getExternalModule() {
        return external;
    }

    public DisplayLists getDisplayListsModule() {
        return displayLists;
    }

    public MentionsModule getMentions() {
        return mentions;
    }

    public void onAppVisible() {
        isAppVisible = true;
        actorApi.forceNetworkCheck();
        analytics.trackAppVisible();
        if (getPresenceModule() != null) {
            getPresenceModule().onAppVisible();
            getNotificationsModule().onAppVisible();
        }
    }

    public void onAppHidden() {
        isAppVisible = false;
        analytics.trackAppHidden();
        if (getPresenceModule() != null) {
            getPresenceModule().onAppHidden();
            getNotificationsModule().onAppHidden();
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

        @Override
        public void onConnectionsChanged(int count) {
            if (appStateModule != null) {
                appStateModule.getAppStateVM().getIsConnecting().change(count == 0);
            }
        }
    }
}