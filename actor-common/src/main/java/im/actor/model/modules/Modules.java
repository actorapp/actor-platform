package im.actor.model.modules;

import im.actor.model.Configuration;
import im.actor.model.network.ActorApi;
import im.actor.model.network.ActorApiCallback;
import im.actor.model.network.Endpoints;
import im.actor.model.storage.PreferenceApiStorage;
import im.actor.model.storage.PreferencesStorage;

/**
 * Created by ex3ndr on 16.02.15.
 */
public class Modules {
    private final Configuration configuration;
    private final ActorApi actorApi;
    private final Auth auth;

    private volatile PreferencesStorage preferences;
    private volatile Users users;
    private volatile Updates updates;
    private volatile Messages messages;
    private volatile Presence presence;
    private volatile Typing typing;

    public Modules(Configuration configuration) {
        this.configuration = configuration;
        this.preferences = configuration.getStorage().createPreferencesStorage();
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
        this.auth = new Auth(this);
    }

    public void onLoggedIn() {
        users = new Users(this);
        messages = new Messages(this);
        updates = new Updates(this);
        presence = new Presence(this);
        typing = new Typing(this);
        messages.run();
        updates.run();
        presence.run();
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
}