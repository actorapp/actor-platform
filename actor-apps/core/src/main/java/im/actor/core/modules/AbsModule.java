/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.core.modules;

import im.actor.core.api.ApiPeer;
import im.actor.core.api.ApiPeerType;
import im.actor.core.entity.Group;
import im.actor.core.entity.Peer;
import im.actor.core.entity.PeerType;
import im.actor.core.entity.User;
import im.actor.core.network.RpcCallback;
import im.actor.core.network.RpcException;
import im.actor.core.network.parser.Request;
import im.actor.core.network.parser.Response;
import im.actor.runtime.actors.ActorRef;
import im.actor.runtime.storage.KeyValueEngine;
import im.actor.runtime.storage.PreferencesStorage;

public abstract class AbsModule {

    public static final String STORAGE_DIALOGS = "dialogs";
    public static final String STORAGE_USERS = "users";
    public static final String STORAGE_GROUPS = "groups";
    public static final String STORAGE_DOWNLOADS = "downloads";
    public static final String STORAGE_CONTACTS = "contacts";
    public static final String STORAGE_NOTIFICATIONS = "notifications";
    public static final String STORAGE_SEARCH = "search";

    public static final String STORAGE_CHAT_PREFIX = "chat_";
    public static final String STORAGE_CHAT_MEDIA_PREFIX = "chat_media_";
    public static final String STORAGE_CHAT_DOCS_PREFIX = "chat_docs_";
    public static final String STORAGE_CHAT_IN = "chat_pending";
    public static final String STORAGE_CHAT_OUT = "chat_pending_out";
    public static final String STORAGE_CURSOR = "chat_cursor";

    private ModuleContext context;

    public AbsModule(ModuleContext context) {
        this.context = context;
    }

    public ModuleContext context() {
        return context;
    }

    public Updates updates() {
        return context.getUpdatesModule();
    }

    public void runOnUiThread(Runnable runnable) {
        im.actor.runtime.Runtime.postToMainThread(runnable);
    }

    public ActorRef sendActor() {
        return context().getMessagesModule().getSendMessageActor();
    }

    public ActorRef dialogsActor() {
        return context().getMessagesModule().getDialogsActor();
    }

    public ActorRef dialogsHistoryActor() {
        return context().getMessagesModule().getDialogsHistoryActor();
    }

    public ActorRef ownReadActor() {
        return context().getMessagesModule().getOwnReadActor();
    }

    public ActorRef plainReceiveActor() {
        return context().getMessagesModule().getPlainReceiverActor();
    }

    public ActorRef conversationActor(Peer peer) {
        return context().getMessagesModule().getConversationActor(peer);
    }

    public ActorRef conversationHistoryActor(Peer peer) {
        return context().getMessagesModule().getConversationHistoryActor(peer);
    }

    public PreferencesStorage preferences() {
        return context.getPreferences();
    }

    public int myUid() {
        return context.getAuthModule().myUid();
    }

    public <T extends Response> void request(Request<T> request, RpcCallback<T> callback) {
        context.getActorApi().request(request, callback);
    }

    public <T extends Response> void request(Request<T> request) {
        context.getActorApi().request(request, new RpcCallback<T>() {
            @Override
            public void onResult(T response) {

            }

            @Override
            public void onError(RpcException e) {

            }
        });
    }

    public KeyValueEngine<User> users() {
        return context.getUsersModule().getUsersStorage();
    }

    public KeyValueEngine<Group> groups() {
        return context.getGroupsModule().getGroups();
    }

    public ApiPeer buildApiPeer(Peer peer) {
        if (peer.getPeerType() == PeerType.PRIVATE) {
            return new ApiPeer(ApiPeerType.PRIVATE, peer.getPeerId());
        } else if (peer.getPeerType() == PeerType.GROUP) {
            return new ApiPeer(ApiPeerType.GROUP, peer.getPeerId());
        } else {
            return null;
        }
    }

    public boolean isValidPeer(Peer peer) {
        if (peer.getPeerType() == PeerType.PRIVATE) {
            return users().getValue(peer.getPeerId()) != null;
        } else if (peer.getPeerType() == PeerType.GROUP) {
            return groups().getValue(peer.getPeerId()) != null;
        }
        return false;
    }
}

