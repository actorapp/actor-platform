package im.actor.model.modules;

import im.actor.model.Configuration;
import im.actor.model.i18n.I18nEngine;
import im.actor.model.log.Log;
import im.actor.model.modules.utils.PreferenceApiStorage;
import im.actor.model.network.ActorApi;
import im.actor.model.network.ActorApiCallback;
import im.actor.model.network.Endpoints;
import im.actor.model.storage.PreferencesStorage;

/**
 * Created by ex3ndr on 16.02.15.
 */
public class Modules {
    private final Configuration configuration;
    private final I18nEngine i18nEngine;
    private final ActorApi actorApi;
    private final Auth auth;

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
    private volatile DisplayLists displayLists;

    public Modules(Configuration configuration) {
        this.configuration = configuration;
        long start = configuration.getThreading().getActorTime();
        this.i18nEngine = new I18nEngine(configuration.getLocaleProvider());
        Log.d("CORE_INIT", "Loading stage5.1 in " + (configuration.getThreading().getActorTime() - start) + " ms");
        this.preferences = configuration.getStorage().createPreferencesStorage();
        Log.d("CORE_INIT", "Loading stage5.2 in " + (configuration.getThreading().getActorTime() - start) + " ms");
        start = configuration.getThreading().getActorTime();
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
                }, configuration.getNetworking());
        Log.d("CORE_INIT", "Loading stage5.3 in " + (configuration.getThreading().getActorTime() - start) + " ms");
        start = configuration.getThreading().getActorTime();
        this.auth = new Auth(this);
        Log.d("CORE_INIT", "Loading stage5.4 in " + (configuration.getThreading().getActorTime() - start) + " ms");
        start = configuration.getThreading().getActorTime();
    }

    public void run() {
        this.auth.run();
    }

    public void onLoggedIn() {
        long start = configuration.getThreading().getActorTime();
        users = new Users(this);
        Log.d("CORE_INIT", "Loading stage6.1 in " + (configuration.getThreading().getActorTime() - start) + " ms");
        start = configuration.getThreading().getActorTime();
        groups = new Groups(this);
        Log.d("CORE_INIT", "Loading stage6.2 in " + (configuration.getThreading().getActorTime() - start) + " ms");
        start = configuration.getThreading().getActorTime();
        messages = new Messages(this);
        Log.d("CORE_INIT", "Loading stage6.3 in " + (configuration.getThreading().getActorTime() - start) + " ms");
        start = configuration.getThreading().getActorTime();
        updates = new Updates(this);
        Log.d("CORE_INIT", "Loading stage6.4 in " + (configuration.getThreading().getActorTime() - start) + " ms");
        start = configuration.getThreading().getActorTime();
        presence = new Presence(this);
        Log.d("CORE_INIT", "Loading stage6.5 in " + (configuration.getThreading().getActorTime() - start) + " ms");
        start = configuration.getThreading().getActorTime();
        typing = new Typing(this);
        Log.d("CORE_INIT", "Loading stage6.6 in " + (configuration.getThreading().getActorTime() - start) + " ms");
        start = configuration.getThreading().getActorTime();
        filesModule = new Files(this);
        Log.d("CORE_INIT", "Loading stage6.6.2 in " + (configuration.getThreading().getActorTime() - start) + " ms");
        start = configuration.getThreading().getActorTime();
        notifications = new Notifications(this);
        Log.d("CORE_INIT", "Loading stage6.7 in " + (configuration.getThreading().getActorTime() - start) + " ms");
        start = configuration.getThreading().getActorTime();
        contacts = new Contacts(this);
        Log.d("CORE_INIT", "Loading stage6.8 in " + (configuration.getThreading().getActorTime() - start) + " ms");
        start = configuration.getThreading().getActorTime();
        settings = new Settings(this);
        profile = new Profile(this);
        displayLists = new DisplayLists(this);

        Log.d("CORE_INIT", "Loading stage6.8.2 in " + (configuration.getThreading().getActorTime() - start) + " ms");
        start = configuration.getThreading().getActorTime();
        filesModule.run();
        Log.d("CORE_INIT", "Loading stage6.9 in " + (configuration.getThreading().getActorTime() - start) + " ms");
        start = configuration.getThreading().getActorTime();
        messages.run();
        Log.d("CORE_INIT", "Loading stage6.10 in " + (configuration.getThreading().getActorTime() - start) + " ms");
        start = configuration.getThreading().getActorTime();
        updates.run();
        Log.d("CORE_INIT", "Loading stage6.11 in " + (configuration.getThreading().getActorTime() - start) + " ms");
        start = configuration.getThreading().getActorTime();
        presence.run();
        Log.d("CORE_INIT", "Loading stage6.12 in " + (configuration.getThreading().getActorTime() - start) + " ms");
        start = configuration.getThreading().getActorTime();

        // Notify about app visible
        presence.onAppVisible();
        notifications.onAppVisible();
    }

    public DisplayLists getDisplayLists() {
        return displayLists;
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
}