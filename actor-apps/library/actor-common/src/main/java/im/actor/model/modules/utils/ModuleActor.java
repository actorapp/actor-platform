/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.model.modules.utils;

import im.actor.model.Configuration;
import im.actor.model.api.OutPeer;
import im.actor.model.droidkit.actors.Actor;
import im.actor.model.droidkit.actors.ActorRef;
import im.actor.model.droidkit.engine.KeyValueEngine;
import im.actor.model.droidkit.engine.ListEngine;
import im.actor.model.entity.Group;
import im.actor.model.entity.Message;
import im.actor.model.entity.Peer;
import im.actor.model.entity.PeerType;
import im.actor.model.entity.User;
import im.actor.model.modules.Modules;
import im.actor.model.modules.Updates;
import im.actor.model.network.RpcCallback;
import im.actor.model.network.RpcException;
import im.actor.model.network.parser.Request;
import im.actor.model.network.parser.Response;
import im.actor.model.droidkit.engine.PreferencesStorage;
import im.actor.model.viewmodel.GroupVM;
import im.actor.model.viewmodel.UserVM;

public class ModuleActor extends Actor {

    protected static final long CURSOR_RECEIVED = 0;
    protected static final long CURSOR_READ = 1;
    protected static final long CURSOR_OWN_READ = 2;
    protected static final long CURSOR_DELETE = 3;

    private Modules modules;

    public ModuleActor(Modules modules) {
        this.modules = modules;
    }

    public OutPeer buidOutPeer(Peer peer) {
        if (peer.getPeerType() == PeerType.PRIVATE) {
            User user = getUser(peer.getPeerId());
            if (user == null) {
                return null;
            }
            return new OutPeer(im.actor.model.api.PeerType.PRIVATE, user.getUid(), user.getAccessHash());
        } else if (peer.getPeerType() == PeerType.GROUP) {
            Group group = getGroup(peer.getPeerId());
            if (group == null) {
                return null;
            }
            return new OutPeer(im.actor.model.api.PeerType.GROUP, group.getGroupId(), group.getAccessHash());
        } else {
            throw new RuntimeException("Unknown peer: " + peer);
        }
    }

    public im.actor.model.api.Peer buildApiPeer(Peer peer) {
        if (peer.getPeerType() == PeerType.PRIVATE) {
            return new im.actor.model.api.Peer(im.actor.model.api.PeerType.PRIVATE, peer.getPeerId());
        } else if (peer.getPeerType() == PeerType.GROUP) {
            return new im.actor.model.api.Peer(im.actor.model.api.PeerType.GROUP, peer.getPeerId());
        } else {
            return null;
        }
    }

    public KeyValueEngine<User> users() {
        return modules.getUsersModule().getUsers();
    }

    public KeyValueEngine<Group> groups() {
        return modules.getGroupsModule().getGroups();
    }

    public Group getGroup(int gid) {
        return groups().getValue(gid);
    }

    public User getUser(int uid) {
        return users().getValue(uid);
    }

    public UserVM getUserVM(int uid) {
        return modules.getUsersModule().getUsersCollection().get(uid);
    }

    public GroupVM getGroupVM(int gid) {
        return modules.getGroupsModule().getGroupsCollection().get(gid);
    }

    public PreferencesStorage preferences() {
        return modules.getPreferences();
    }

    public Configuration config() {
        return modules.getConfiguration();
    }

    public Updates updates() {
        return modules.getUpdatesModule();
    }

    public ListEngine<Message> messages(Peer peer) {
        return modules.getMessagesModule().getConversationEngine(peer);
    }
    public ListEngine<Message> media(Peer peer){
        return modules.getMessagesModule().getMediaEngine(peer);
    }
    public int myUid() {
        return modules.getAuthModule().myUid();
    }

    public Modules modules() {
        return modules;
    }

    public ActorRef getConversationActor(final Peer peer) {
        return modules().getMessagesModule().getConversationActor(peer);
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
        modules.getActorApi().request(request, new RpcCallback<T>() {
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
