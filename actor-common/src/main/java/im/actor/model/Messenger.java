package im.actor.model;

import im.actor.model.modules.Auth;
import im.actor.model.modules.Updates;
import im.actor.model.modules.Users;
import im.actor.model.mvvm.KeyValueEngine;
import im.actor.model.network.ActorApi;
import im.actor.model.network.ActorApiCallback;
import im.actor.model.storage.PreferenceApiStorage;

/**
 * Created by ex3ndr on 08.02.15.
 */
public class Messenger {
    private Configuration configuration;
    private Auth auth;
    private volatile Users users;
    private volatile Updates updates;
    private ActorApi actorApi;

    public Messenger(Configuration configuration) {
        this.configuration = configuration;
        this.actorApi = new ActorApi(configuration.getEndpoints(),
                new PreferenceApiStorage(configuration.getPreferencesStorage()),
                new ActorApiCallback() {
                    @Override
                    public void onAuthIdInvalidated(long authKey) {

                    }

                    @Override
                    public void onNewSessionCreated() {
                        if (updates != null) {
                            updates.onSessionCreated();
                        }
                    }

                    @Override
                    public void onUpdateReceived(Object obj) {
                        if (updates != null) {
                            updates.onUpdateReceived(obj);
                        }
                    }
                });
        this.auth = new Auth(this);
    }

    public void onLoggedIn() {
        users = new Users(this);
        updates = new Updates(this);
    }

    public Updates getUpdates() {
        return updates;
    }

    public KeyValueEngine<im.actor.model.entity.User> getUsers() {
        return users.getUsers();
    }

    public Configuration getConfiguration() {
        return configuration;
    }

    public ActorApi getActorApi() {
        return actorApi;
    }

    public Auth getAuth() {
        return auth;
    }

    public State getState() {
        return auth.getState();
    }
}