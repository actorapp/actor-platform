/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.core.modules;

import im.actor.core.entity.Group;
import im.actor.core.entity.Peer;
import im.actor.core.entity.User;
import im.actor.core.network.RpcCallback;
import im.actor.core.network.RpcException;
import im.actor.core.network.parser.Request;
import im.actor.core.network.parser.Response;
import im.actor.runtime.actors.ActorRef;
import im.actor.runtime.storage.KeyValueEngine;
import im.actor.runtime.storage.PreferencesStorage;

public class BaseModule {

    public static final String STORAGE_DIALOGS = "dialogs";
    public static final String STORAGE_USERS = "users";
    public static final String STORAGE_GROUPS = "groups";
    public static final String STORAGE_DOWNLOADS = "downloads";
    public static final String STORAGE_CONTACTS = "contacts";
    public static final String STORAGE_NOTIFICATIONS = "notifications";
    public static final String STORAGE_SEARCH = "search";

    public static final String STORAGE_CHAT_PREFIX = "chat_";
    public static final String STORAGE_CHAT_MEDIA_PREFIX = "chat_media_";
    public static final String STORAGE_CHAT_IN = "chat_pending";
    public static final String STORAGE_CHAT_OUT = "chat_pending_out";
    public static final String STORAGE_CURSOR = "chat_cursor";

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
        im.actor.runtime.Runtime.postToMainThread(runnable);
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

