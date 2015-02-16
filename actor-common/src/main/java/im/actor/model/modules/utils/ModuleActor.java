package im.actor.model.modules.utils;

import im.actor.model.droidkit.actors.Actor;
import im.actor.model.Messenger;
import im.actor.model.entity.Message;
import im.actor.model.entity.Peer;
import im.actor.model.entity.User;
import im.actor.model.modules.Modules;
import im.actor.model.modules.Updates;
import im.actor.model.mvvm.KeyValueEngine;
import im.actor.model.mvvm.ListEngine;
import im.actor.model.network.RpcCallback;
import im.actor.model.network.RpcException;
import im.actor.model.network.parser.Request;
import im.actor.model.network.parser.Response;
import im.actor.model.storage.PreferencesStorage;

/**
 * Created by ex3ndr on 08.02.15.
 */
public class ModuleActor extends Actor {
    private Modules messenger;

    public ModuleActor(Modules messenger) {
        this.messenger = messenger;
    }

    public KeyValueEngine<User> users() {
        return messenger.getUsersModule().getUsers();
    }

    public User getUser(int uid) {
        return users().getValue(uid);
    }

    public PreferencesStorage preferences() {
        return messenger.getConfiguration().getPreferencesStorage();
    }

    public Updates updates() {
        return messenger.getUpdatesModule();
    }

    public ListEngine<Message> messages(Peer peer) {
        return messenger.getMessagesModule().getConversationEngine(peer);
    }

    public int myUid() {
        return messenger.getAuthModule().myUid();
    }

    public Modules modules() {
        return messenger;
    }

    public <T extends Response> void request(Request<T> request) {
        request(request, new RpcCallback<T>() {
            @Override
            public void onResult(T response) {

            }

            @Override
            public void onError(RpcException e) {

            }
        });
    }

    public <T extends Response> void request(Request<T> request, final RpcCallback<T> callback) {
        messenger.getActorApi().request(request, new RpcCallback<T>() {
            @Override
            public void onResult(final T response) {
                self().send(new Runnable() {
                    @Override
                    public void run() {
                        callback.onResult(response);
                    }
                });
            }

            @Override
            public void onError(final RpcException e) {
                self().send(new Runnable() {
                    @Override
                    public void run() {
                        callback.onError(e);
                    }
                });
            }
        });
    }
}
