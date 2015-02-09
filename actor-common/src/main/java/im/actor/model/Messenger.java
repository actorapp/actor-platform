package im.actor.model;

import im.actor.model.entity.Dialog;
import im.actor.model.entity.Message;
import im.actor.model.entity.Peer;
import im.actor.model.modules.Auth;
import im.actor.model.modules.Messages;
import im.actor.model.modules.Updates;
import im.actor.model.modules.Users;
import im.actor.model.mvvm.KeyValueEngine;
import im.actor.model.mvvm.ListEngine;
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
    private volatile Messages messages;
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
        messages = new Messages(this);
        updates = new Updates(this);
    }

    public int myUid() {
        return auth.myUid();
    }

    public Updates getUpdatesModule() {
        return updates;
    }

    public Messages getMessagesModule() {
        return messages;
    }

    public ListEngine<Message> getMessages(Peer peer) {
        return messages.getConversationEngine(peer);
    }

    public ListEngine<Dialog> getDialogs() {
        return messages.getDialogsEngine();
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