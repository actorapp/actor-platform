/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.core.modules;

import im.actor.core.Configuration;
import im.actor.core.Messenger;
import im.actor.core.i18n.I18nEngine;
import im.actor.core.modules.api.ApiModule;
import im.actor.core.modules.auth.Authentication;
import im.actor.core.modules.eventbus.EventBusModule;
import im.actor.core.modules.sequence.Updates;
import im.actor.core.modules.misc.AppStateModule;
import im.actor.core.modules.calls.CallsModule;
import im.actor.core.modules.contacts.ContactsModule;
import im.actor.core.modules.misc.DeviceInfoModule;
import im.actor.core.modules.misc.DisplayLists;
import im.actor.core.modules.encryption.EncryptionModule;
import im.actor.core.modules.external.ExternalModule;
import im.actor.core.modules.file.FilesModule;
import im.actor.core.modules.groups.GroupsModule;
import im.actor.core.modules.mentions.MentionsModule;
import im.actor.core.modules.messaging.MessagesModule;
import im.actor.core.modules.notifications.NotificationsModule;
import im.actor.core.modules.presence.PresenceModule;
import im.actor.core.modules.profile.ProfileModule;
import im.actor.core.modules.push.PushesModule;
import im.actor.core.modules.search.SearchModule;
import im.actor.core.modules.security.SecurityModule;
import im.actor.core.modules.settings.SettingsModule;
import im.actor.core.modules.stickers.StickersModule;
import im.actor.core.modules.storage.StorageModule;
import im.actor.core.modules.typing.TypingModule;
import im.actor.core.modules.users.UsersModule;
import im.actor.core.network.ActorApi;
import im.actor.core.util.Timing;
import im.actor.runtime.Runtime;
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
    private final StorageModule storageModule;

    // API support
    private final ApiModule api;

    // Modules required before authentication
    private final ExternalModule external;
    private final Authentication authentication;

    // Modules for authenticated users
    private volatile Updates updates;
    private volatile UsersModule users;
    private volatile GroupsModule groups;
    private volatile AppStateModule appStateModule;
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
    private volatile EventBusModule eventBusModule;

    public Modules(Messenger messenger, Configuration configuration) {
        this.messenger = messenger;
        this.configuration = configuration;

        // Timing timing = new Timing("MODULES_INIT");

        // timing.section("I18N");
        this.i18nEngine = I18nEngine.create(this);

        // timing.section("Preferences");
        this.preferences = Storage.createPreferencesStorage();

        // timing.section("Storage");
        this.storageModule = new StorageModule(this);

        // timing.section("Events");
        this.events = new EventBus();

        // timing.section("App State");
        appStateModule = new AppStateModule(this);

        // timing.section("API");
        this.api = new ApiModule(this);

        // timing.section("External");
        this.external = new ExternalModule(this);

        // timing.section("Pushes");
        this.pushes = new PushesModule(this);

        // timing.section("Auth");
        this.authentication = new Authentication(this);
        // timing.end();
    }

    public void run() {
        // Timing timing = new Timing("RUN");
        // timing.section("Auth");
        this.authentication.run();
        // timing.end();
    }

    public void onLoggedIn(boolean first) {
        Timing timing = new Timing("ACCOUNT_CREATE");
        timing.section("Users");
        users = new UsersModule(this);
        timing.section("Storage");
        storageModule.run(first);
        timing.section("Groups");
        groups = new GroupsModule(this);
        timing.section("App State");
        appStateModule.run();
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
//        timing.section("Encryption");
//        encryptionModule = new EncryptionModule(this);
        timing.section("DisplayLists");
        displayLists = new DisplayLists(this);
        timing.section("DeviceInfo");
        deviceInfoModule = new DeviceInfoModule(this);
        timing.section("EventBus");
        eventBusModule = new EventBusModule(this);


        timing = new Timing("ACCOUNT_RUN");
        timing.section("Users");
        users.run();
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
//        timing.section("Encryption");
//        encryptionModule.run();
        timing.section("Contacts");
        contacts.run();
        timing.section("Messages");
        messages.run();
        timing.section("EventBus");
        eventBusModule.run();
        timing.section("Updates");
        updates.run();
        timing.section("Calls");
        calls.run();
        timing.section("Stickers");
        stickers.run();
        timing.end();

        if (Runtime.isMainThread()) {
            messenger.onLoggedIn();
        } else {
            Runtime.postToMainThread(new Runnable() {
                @Override
                public void run() {
                    messenger.onLoggedIn();
                }
            });
        }
    }

    @Override
    public void afterStorageReset() {
        // Recreation of Users Module to pick fresh database
        users = new UsersModule(this);
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
    public PreferencesStorage getPreferences() {
        return preferences;
    }

    @Override
    public Configuration getConfiguration() {
        return configuration;
    }

    @Override
    public Authentication getAuthModule() {
        return authentication;
    }

    @Override
    public UsersModule getUsersModule() {
        return users;
    }

    @Override
    public GroupsModule getGroupsModule() {
        return groups;
    }

    @Override
    public StickersModule getStickersModule() {
        return stickers;
    }

    @Override
    public CallsModule getCallsModule() {
        return calls;
    }

    @Override
    public MessagesModule getMessagesModule() {
        return messages;
    }

    @Override
    public Updates getUpdatesModule() {
        return updates;
    }

    @Override
    public TypingModule getTypingModule() {
        return typing;
    }

    @Override
    public PresenceModule getPresenceModule() {
        return presence;
    }

    @Override
    public ActorApi getActorApi() {
        return api.getActorApi();
    }

    @Override
    public ApiModule getApiModule() {
        return api;
    }

    @Override
    public StorageModule getStorageModule() {
        return storageModule;
    }


    @Override
    public I18nEngine getI18nModule() {
        return i18nEngine;
    }

    @Override
    public ContactsModule getContactsModule() {
        return contacts;
    }

    @Override
    public FilesModule getFilesModule() {
        return filesModule;
    }

    @Override
    public NotificationsModule getNotificationsModule() {
        return notifications;
    }

    @Override
    public SettingsModule getSettingsModule() {
        return settings;
    }

    @Override
    public ProfileModule getProfileModule() {
        return profile;
    }

    @Override
    public AppStateModule getAppStateModule() {
        return appStateModule;
    }

    @Override
    public PushesModule getPushesModule() {
        return pushes;
    }

    @Override
    public SecurityModule getSecurityModule() {
        return security;
    }

    @Override
    public SearchModule getSearchModule() {
        return search;
    }

    @Override
    public Messenger getMessenger() {
        return messenger;
    }

    @Override
    public ExternalModule getExternalModule() {
        return external;
    }

    @Override
    public DisplayLists getDisplayListsModule() {
        return displayLists;
    }

    @Override
    public MentionsModule getMentions() {
        return mentions;
    }

    @Override
    public DeviceInfoModule getDeviceInfoModule() {
        return deviceInfoModule;
    }

    @Override
    public EncryptionModule getEncryption() {
        return encryptionModule;
    }

    @Override
    public EventBusModule getEventBus() {
        return eventBusModule;
    }

    @Override
    public EventBus getEvents() {
        return events;
    }
}