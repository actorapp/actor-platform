/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.core.modules.utils;

import im.actor.core.Configuration;
import im.actor.core.api.OutPeer;
import im.actor.core.entity.Group;
import im.actor.core.entity.Peer;
import im.actor.core.entity.PeerType;
import im.actor.core.entity.User;
import im.actor.core.modules.ModuleContext;
import im.actor.core.modules.Updates;
import im.actor.core.network.RpcCallback;
import im.actor.core.network.RpcException;
import im.actor.core.network.parser.Request;
import im.actor.core.network.parser.Response;
import im.actor.core.viewmodel.GroupVM;
import im.actor.core.viewmodel.UserVM;
import im.actor.runtime.actors.Actor;
import im.actor.runtime.storage.KeyValueEngine;
import im.actor.runtime.storage.PreferencesStorage;

public class ModuleActor extends Actor {

    protected static final long CURSOR_RECEIVED = 0;
    protected static final long CURSOR_READ = 1;
    protected static final long CURSOR_OWN_READ = 2;
    protected static final long CURSOR_DELETE = 3;

    private ModuleContext context;

    public ModuleActor(ModuleContext context) {
        this.context = context;
    }

    public OutPeer buidOutPeer(Peer peer) {
        if (peer.getPeerType() == PeerType.PRIVATE) {
            User user = getUser(peer.getPeerId());
            if (user == null) {
                return null;
            }
            return new OutPeer(im.actor.core.api.PeerType.PRIVATE, user.getUid(), user.getAccessHash());
        } else if (peer.getPeerType() == PeerType.GROUP) {
            Group group = getGroup(peer.getPeerId());
            if (group == null) {
                return null;
            }
            return new OutPeer(im.actor.core.api.PeerType.GROUP, group.getGroupId(), group.getAccessHash());
        } else {
            throw new RuntimeException("Unknown peer: " + peer);
        }
    }

    public im.actor.core.api.Peer buildApiPeer(Peer peer) {
        if (peer.getPeerType() == PeerType.PRIVATE) {
            return new im.actor.core.api.Peer(im.actor.core.api.PeerType.PRIVATE, peer.getPeerId());
        } else if (peer.getPeerType() == PeerType.GROUP) {
            return new im.actor.core.api.Peer(im.actor.core.api.PeerType.GROUP, peer.getPeerId());
        } else {
            return null;
        }
    }

    public KeyValueEngine<User> users() {
        return context.getUsersModule().getUsersStorage();
    }

    public KeyValueEngine<Group> groups() {
        return context.getGroupsModule().getGroups();
    }

    public Group getGroup(int gid) {
        return groups().getValue(gid);
    }

    public User getUser(int uid) {
        return users().getValue(uid);
    }

    public UserVM getUserVM(int uid) {
        return context.getUsersModule().getUsers().get(uid);
    }

    public GroupVM getGroupVM(int gid) {
        return context.getGroupsModule().getGroupsCollection().get(gid);
    }

    public PreferencesStorage preferences() {
        return context.getPreferences();
    }

    public Configuration config() {
        return context.getConfiguration();
    }

    public Updates updates() {
        return context.getUpdatesModule();
    }

    public int myUid() {
        return context.getAuthModule().myUid();
    }

    public ModuleContext context() {
        return context;
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

    public <T extends Response> void request(final Request<T> request, final RpcCallback<T> callback) {
        context.getActorApi().request(request, new RpcCallback<T>() {
            @Override
            public void onResult(final T response) {
                self().send(new Runnable() {
                    @Override
                    public void run() {
                        callback.onResult(response);
                    }

                    @Override
                    public String toString() {
                        return "Response {" + response + "}";
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

                    @Override
                    public String toString() {
                        return "Error {" + e + "}";
                    }
                });
            }
        });
    }
}
