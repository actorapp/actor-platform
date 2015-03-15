package im.actor.model.modules;

import im.actor.model.CryptoProvider;
import im.actor.model.Storage;
import im.actor.model.droidkit.actors.ActorRef;
import im.actor.model.entity.Group;
import im.actor.model.entity.Peer;
import im.actor.model.entity.User;
import im.actor.model.network.RpcCallback;
import im.actor.model.network.RpcException;
import im.actor.model.network.parser.Request;
import im.actor.model.network.parser.Response;
import im.actor.model.droidkit.engine.KeyValueEngine;
import im.actor.model.droidkit.engine.PreferencesStorage;

/**
 * Created by ex3ndr on 16.02.15.
 */
public class BaseModule {

    public static final String STORAGE_DIALOGS = "dialogs";
    public static final String STORAGE_USERS = "users";
    public static final String STORAGE_GROUPS = "groups";
    public static final String STORAGE_DOWNLOADS = "downloads";
    public static final String STORAGE_CONTACTS = "contacts";

    public static final String STORAGE_CHAT_PREFIX = "chat_";

    private Modules modules;

    public BaseModule(Modules modules) {
        this.modules = modules;
    }

    public Modules modules() {
        return modules;
    }

    public Updates updates() {
        return modules.getUpdatesModule();
    }

    public void runOnUiThread(Runnable runnable) {
        modules.getConfiguration().getMainThread().runOnUiThread(runnable);
    }

    public ActorRef sendActor() {
        return modules().getMessagesModule().getSendMessageActor();
    }

    public ActorRef dialogsActor() {
        return modules().getMessagesModule().getDialogsActor();
    }

    public ActorRef dialogsHistoryActor() {
        return modules().getMessagesModule().getDialogsHistoryActor();
    }

    public ActorRef ownReadActor() {
        return modules().getMessagesModule().getOwnReadActor();
    }

    public ActorRef plainReceiveActor() {
        return modules().getMessagesModule().getPlainReceiverActor();
    }

    public ActorRef conversationActor(Peer peer) {
        return modules().getMessagesModule().getConversationActor(peer);
    }

    public ActorRef conversationHistoryActor(Peer peer) {
        return modules().getMessagesModule().getConversationHistoryActor(peer);
    }

    public PreferencesStorage preferences() {
        return modules.getPreferences();
    }

    public CryptoProvider crypto() {
        return modules.getConfiguration().getCryptoProvider();
    }

    public Storage storage() {
        return modules.getConfiguration().getStorage();
    }

    public int myUid() {
        return modules.getAuthModule().myUid();
    }

    public <T extends Response> void request(Request<T> request, RpcCallback<T> callback) {
        modules.getActorApi().request(request, callback);
    }

    public <T extends Response> void request(Request<T> request) {
        modules.getActorApi().request(request, new RpcCallback<T>() {
            @Override
            public void onResult(T response) {

            }

            @Override
            public void onError(RpcException e) {

            }
        });
    }

    public KeyValueEngine<User> users() {
        return modules.getUsersModule().getUsers();
    }

    public KeyValueEngine<Group> groups() {
        return modules.getGroupsModule().getGroups();
    }
}

