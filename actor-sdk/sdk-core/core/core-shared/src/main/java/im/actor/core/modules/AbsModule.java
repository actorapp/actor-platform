/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.core.modules;

import im.actor.core.api.ApiOutPeer;
import im.actor.core.api.ApiPeer;
import im.actor.core.api.ApiPeerType;
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
import im.actor.runtime.mtproto.ManagedConnection;
import im.actor.runtime.storage.KeyValueEngine;
import im.actor.runtime.storage.KeyValueStorage;
import im.actor.runtime.storage.PreferencesStorage;

public abstract class AbsModule {

    public static final int RPC_TIMEOUT = (int) (1.1 * ManagedConnection.CONNECTION_TIMEOUT);

    public static final String STORAGE_DIALOGS = "dialogs";
    public static final String STORAGE_DIALOGS_DESC = "dialogs_desc";
    public static final String STORAGE_USERS = "users";
    public static final String STORAGE_GROUPS = "groups";
    public static final String STORAGE_DOWNLOADS = "downloads";
    public static final String STORAGE_CONTACTS = "contacts";
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

    public ApiOutPeer buildApiOutPeer(Peer peer) {
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

    public boolean isValidPeer(Peer peer) {
        if (peer.getPeerType() == PeerType.PRIVATE) {
            return users().getValue(peer.getPeerId()) != null;
        } else if (peer.getPeerType() == PeerType.GROUP) {
            return groups().getValue(peer.getPeerId()) != null;
        }
        return false;
    }


}

