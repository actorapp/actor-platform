/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.core.modules;

import im.actor.core.Configuration;
import im.actor.core.Messenger;
import im.actor.core.i18n.I18nEngine;
import im.actor.core.modules.api.ApiModule;
import im.actor.core.modules.sequence.Updates;
import im.actor.core.modules.internal.AppStateModule;
import im.actor.core.modules.internal.CallsModule;
import im.actor.core.modules.internal.ContactsModule;
import im.actor.core.modules.internal.DeviceInfoModule;
import im.actor.core.modules.internal.DisplayLists;
import im.actor.core.modules.encryption.EncryptionModule;
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
import im.actor.core.modules.internal.StickersModule;
import im.actor.core.modules.internal.TypingModule;
import im.actor.core.modules.users.UsersModule;
import im.actor.core.network.ActorApi;
import im.actor.core.util.Timing;
import im.actor.runtime.Storage;
import im.actor.runtime.eventbus.EventBus;
import im.actor.runtime.storage.PreferencesStorage;

public class Modules implements ModuleContext {

    // Messenger object
    private final Messenger messenger;

    // Very basic modules
    private final Configuration configuration;
    private final I18nEngine i18nEngine;
    private final PreferencesStorage preferences;
    private final EventBus events;

    // API support
    private final ApiModule api;

    // Modules required before authentication
    private final AppStateModule appStateModule;
    private final ExternalModule external;
    private final Authentication authentication;

    // Modules for authenticated users
    private volatile Updates updates;
    private volatile UsersModule users;
    private volatile GroupsModule groups;
    private volatile StickersModule stickers;
    private volatile CallsModule calls;
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
    private volatile EncryptionModule encryptionModule;
    private volatile DeviceInfoModule deviceInfoModule;

    public Modules(Messenger messenger, Configuration configuration) {
        this.messenger = messenger;
        this.configuration = configuration;

        Timing timing = new Timing("MODULES_INIT");

        timing.section("I18N");
        this.i18nEngine = new I18nEngine(this);

        timing.section("Preferences");
        this.preferences = Storage.createPreferencesStorage();

        timing.section("Events");
        this.events = new EventBus();

        timing.section("API");
        this.api = new ApiModule(this);

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

    public void onLoggedIn() {
        Timing timing = new Timing("ACCOUNT_CREATE");
        timing.section("Users");
        users = new UsersModule(this);
        timing.section("Groups");
        groups = new GroupsModule(this);
        timing.section("Stickers");
        stickers = new StickersModule(this);
        timing.section("Calls");
        calls = new CallsModule(this);
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
        timing.section("Encryption");
        encryptionModule = new EncryptionModule(this);
        timing.section("DisplayLists");
        displayLists = new DisplayLists(this);
        timing.section("DeviceInfo");
        deviceInfoModule = new DeviceInfoModule(this);
        timing.end();


        timing = new Timing("ACCOUNT_RUN");
        timing.section("Settings");
        settings.run();
        timing.section("DeviceInfo");
        deviceInfoModule.run();
        timing.section("Files");
        filesModule.run();
        timing.section("Search");
        search.run();
        timing.section("Notifications");
        notifications.run();
        timing.section("AppState");
        appStateModule.run();
        timing.section("Encryption");
        encryptionModule.run();
        timing.section("Contacts");
        contacts.run();
        timing.section("Messages");
        messages.run();
        timing.section("Updates");
        updates.run();
        timing.section("Calls");
        calls.run();
        timing.end();

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

    public StickersModule getStickersModule() {
        return stickers;
    }

    public CallsModule getCallsModule() {
        return calls;
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
        return api.getActorApi();
    }

    @Override
    public ApiModule getApiModule() {
        return api;
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

    public ExternalModule getExternalModule() {
        return external;
    }

    public DisplayLists getDisplayListsModule() {
        return displayLists;
    }

    public MentionsModule getMentions() {
        return mentions;
    }

    public DeviceInfoModule getDeviceInfoModule() {
        return deviceInfoModule;
    }

    @Override
    public EncryptionModule getEncryption() {
        return encryptionModule;
    }

    public EventBus getEvents() {
        return events;
    }
}