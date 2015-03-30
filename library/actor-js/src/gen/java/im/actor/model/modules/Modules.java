package im.actor.model.modules;

import im.actor.model.Configuration;
import im.actor.model.i18n.I18nEngine;
import im.actor.model.log.Log;
import im.actor.model.modules.utils.PreferenceApiStorage;
import im.actor.model.network.ActorApi;
import im.actor.model.network.ActorApiCallback;
import im.actor.model.network.Endpoints;
import im.actor.model.droidkit.engine.PreferencesStorage;

/**
 * Created by ex3ndr on 16.02.15.
 */
public class Modules {
    private final Configuration configuration;
    private final I18nEngine i18nEngine;
    private final ActorApi actorApi;
    private final Auth auth;
    private final AppStateModule appStateModule;

    private volatile PreferencesStorage preferences;
    private volatile Users users;
    private volatile Groups groups;
    private volatile Updates updates;
    private volatile Messages messages;
    private volatile Presence presence;
    private volatile Typing typing;
    private volatile Files filesModule;
    private volatile Contacts contacts;
    private volatile Notifications notifications;
    private volatile Settings settings;
    private volatile Profile profile;

    public Modules(Configuration configuration) {
        this.configuration = configuration;
        long start = configuration.getThreadingProvider().getActorTime();
        this.i18nEngine = new I18nEngine(configuration.getLocaleProvider(), this);
        Log.d("CORE_INIT", "Loading stage5.1 in " + (configuration.getThreadingProvider().getActorTime() - start) + " ms");
        this.preferences = configuration.getStorageProvider().createPreferencesStorage();
        Log.d("CORE_INIT", "Loading stage5.2 in " + (configuration.getThreadingProvider().getActorTime() - start) + " ms");
        start = configuration.getThreadingProvider().getActorTime();
        this.actorApi = new ActorApi(new Endpoints(configuration.getEndpoints()),
                new PreferenceApiStorage(preferences),
                new ActorApiCallback() {
                    @Override
                    public void onAuthIdInvalidated(long authKey) {

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
                }, configuration.getNetworkProvider());
        Log.d("CORE_INIT", "Loading stage5.3 in " + (configuration.getThreadingProvider().getActorTime() - start) + " ms");
        start = configuration.getThreadingProvider().getActorTime();
        this.auth = new Auth(this);
        Log.d("CORE_INIT", "Loading stage5.4 in " + (configuration.getThreadingProvider().getActorTime() - start) + " ms");
        start = configuration.getThreadingProvider().getActorTime();
        this.appStateModule = new AppStateModule(this);
    }

    public void run() {
        this.auth.run();
    }

    public void onLoggedIn() {
        long start = configuration.getThreadingProvider().getActorTime();
        users = new Users(this);
        Log.d("CORE_INIT", "Loading stage6.1 in " + (configuration.getThreadingProvider().getActorTime() - start) + " ms");
        start = configuration.getThreadingProvider().getActorTime();
        groups = new Groups(this);
        Log.d("CORE_INIT", "Loading stage6.2 in " + (configuration.getThreadingProvider().getActorTime() - start) + " ms");
        start = configuration.getThreadingProvider().getActorTime();
        messages = new Messages(this);
        Log.d("CORE_INIT", "Loading stage6.3 in " + (configuration.getThreadingProvider().getActorTime() - start) + " ms");
        start = configuration.getThreadingProvider().getActorTime();
        updates = new Updates(this);
        Log.d("CORE_INIT", "Loading stage6.4 in " + (configuration.getThreadingProvider().getActorTime() - start) + " ms");
        start = configuration.getThreadingProvider().getActorTime();
        presence = new Presence(this);
        Log.d("CORE_INIT", "Loading stage6.5 in " + (configuration.getThreadingProvider().getActorTime() - start) + " ms");
        start = configuration.getThreadingProvider().getActorTime();
        typing = new Typing(this);
        Log.d("CORE_INIT", "Loading stage6.6 in " + (configuration.getThreadingProvider().getActorTime() - start) + " ms");
        start = configuration.getThreadingProvider().getActorTime();
        filesModule = new Files(this);
        Log.d("CORE_INIT", "Loading stage6.6.2 in " + (configuration.getThreadingProvider().getActorTime() - start) + " ms");
        start = configuration.getThreadingProvider().getActorTime();
        notifications = new Notifications(this);
        Log.d("CORE_INIT", "Loading stage6.7 in " + (configuration.getThreadingProvider().getActorTime() - start) + " ms");
        start = configuration.getThreadingProvider().getActorTime();
        contacts = new Contacts(this);
        Log.d("CORE_INIT", "Loading stage6.8 in " + (configuration.getThreadingProvider().getActorTime() - start) + " ms");
        start = configuration.getThreadingProvider().getActorTime();
        settings = new Settings(this);
        profile = new Profile(this);

        Log.d("CORE_INIT", "Loading stage6.8.2 in " + (configuration.getThreadingProvider().getActorTime() - start) + " ms");
        start = configuration.getThreadingProvider().getActorTime();
        filesModule.run();
        Log.d("CORE_INIT", "Loading stage6.9 in " + (configuration.getThreadingProvider().getActorTime() - start) + " ms");
        start = configuration.getThreadingProvider().getActorTime();
        notifications.run();
        messages.run();
        Log.d("CORE_INIT", "Loading stage6.10 in " + (configuration.getThreadingProvider().getActorTime() - start) + " ms");
        start = configuration.getThreadingProvider().getActorTime();
        updates.run();
        Log.d("CORE_INIT", "Loading stage6.11 in " + (configuration.getThreadingProvider().getActorTime() - start) + " ms");
        start = configuration.getThreadingProvider().getActorTime();
        presence.run();
        Log.d("CORE_INIT", "Loading stage6.12 in " + (configuration.getThreadingProvider().getActorTime() - start) + " ms");
        start = configuration.getThreadingProvider().getActorTime();

        // Notify about app visible
        presence.onAppVisible();
        notifications.onAppVisible();
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
}