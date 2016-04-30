/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.core.modules;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import im.actor.core.api.ApiGroupOutPeer;
import im.actor.core.api.ApiOutPeer;
import im.actor.core.api.ApiPeer;
import im.actor.core.api.ApiPeerType;
import im.actor.core.api.ApiUserOutPeer;
import im.actor.core.api.rpc.RequestEditUserLocalName;
import im.actor.core.api.rpc.RequestGetReferencedEntitites;
import im.actor.core.api.rpc.ResponseSeq;
import im.actor.core.entity.Group;
import im.actor.core.entity.Peer;
import im.actor.core.entity.PeerType;
import im.actor.core.entity.User;
import im.actor.core.modules.sequence.Updates;
import im.actor.core.network.RpcCallback;
import im.actor.core.network.RpcException;
import im.actor.core.network.parser.Request;
import im.actor.core.network.parser.Response;
import im.actor.runtime.actors.ActorRef;
import im.actor.runtime.actors.messages.Void;
import im.actor.runtime.function.Function;
import im.actor.runtime.function.Tuple2;
import im.actor.runtime.mtproto.ManagedConnection;
import im.actor.runtime.promise.Promise;
import im.actor.runtime.promise.PromiseFunc;
import im.actor.runtime.promise.PromiseResolver;
import im.actor.runtime.promise.Promises;
import im.actor.runtime.promise.PromisesArray;
import im.actor.runtime.storage.KeyValueEngine;
import im.actor.runtime.storage.KeyValueStorage;
import im.actor.runtime.storage.PreferencesStorage;

public abstract class AbsModule {

    public static final int RPC_TIMEOUT = (int) (1.1 * ManagedConnection.CONNECTION_TIMEOUT);

    public static final String STORAGE_DIALOGS = "dialogs";
    public static final String STORAGE_USERS = "users";
    public static final String STORAGE_GROUPS = "groups";
    public static final String STORAGE_DOWNLOADS = "downloads";
    public static final String STORAGE_CONTACTS = "contacts";
    public static final String STORAGE_PHONE_BOOK = "phone_book";
    public static final String STORAGE_NOTIFICATIONS = "notifications";
    public static final String STORAGE_SEARCH = "search";

    public static final String STORAGE_BOOK_IMPORT = "book_import";

    public static final String STORAGE_CHAT_STATES = "chat_states";
    public static final String STORAGE_CHAT_PREFIX = "chat_";
    public static final String STORAGE_CHAT_DOCS_PREFIX = "chat_docs_";
    public static final String STORAGE_CURSOR = "chat_cursor";

    public static final String STORAGE_BLOB = "blob";

    public static final long BLOB_DIALOGS_ACTIVE = 0;

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

    public ActorRef stickersActor() {
        return context().getStickersModule().getStickersActor();
    }

    public PreferencesStorage preferences() {
        return context.getPreferences();
    }

    public int myUid() {
        return context.getAuthModule().myUid();
    }

    public <T extends Response> void request(Request<T> request, RpcCallback<T> callback, long timeout) {
        context.getActorApi().request(request, callback, timeout);
    }

    public <T extends Response> void request(Request<T> request, RpcCallback<T> callback) {
        request(request, callback, 0);
    }

    public <T extends Response> void request(Request<T> request, long timeout) {
        context.getActorApi().request(request, new RpcCallback<T>() {
            @Override
            public void onResult(T response) {

            }

            @Override
            public void onError(RpcException e) {

            }
        }, timeout);
    }

    public <T extends Response> Promise<T> api(Request<T> request) {
        return new Promise<>((PromiseFunc<T>) resolver -> {
            request(request, new RpcCallback<T>() {
                @Override
                public void onResult(T response) {
                    resolver.result(response);
                }

                @Override
                public void onError(RpcException e) {
                    resolver.error(e);
                }
            });
        });
    }

    public <T extends Response> void request(Request<T> request) {
        request(request, 0);
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

    public ApiOutPeer getApiOutPeer(Peer peer) {
        if (peer.getPeerType() == PeerType.PRIVATE) {
            return new ApiOutPeer(ApiPeerType.PRIVATE, peer.getPeerId(),
                    users().getValue(peer.getPeerId()).getAccessHash());
        } else if (peer.getPeerType() == PeerType.GROUP) {
            return new ApiOutPeer(ApiPeerType.GROUP, peer.getPeerId(),
                    groups().getValue(peer.getPeerId()).getAccessHash());
        } else {
            return null;
        }
    }

    public Promise<ApiOutPeer> buildOutPeer(Peer peer) {
        return new Promise<>((PromiseFunc<ApiOutPeer>) resolver -> {
            if (peer.getPeerType() == PeerType.PRIVATE) {
                users().getValueAsync(peer.getPeerId())
                        .map(user -> new ApiOutPeer(ApiPeerType.PRIVATE, user.getUid(), user.getAccessHash()))
                        .pipeTo(resolver);
            } else if (peer.getPeerType() == PeerType.GROUP) {
                groups().getValueAsync(peer.getPeerId())
                        .map(group -> new ApiOutPeer(ApiPeerType.GROUP, group.getGroupId(), group.getAccessHash()))
                        .pipeTo(resolver);
            } else {
                throw new RuntimeException("Unknown peer: " + peer);
            }
        });
    }
}

