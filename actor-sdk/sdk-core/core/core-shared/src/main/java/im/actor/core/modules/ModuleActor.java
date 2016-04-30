/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.core.modules;

import java.util.List;

import im.actor.core.Configuration;
import im.actor.core.api.ApiGroupOutPeer;
import im.actor.core.api.ApiPeer;
import im.actor.core.api.ApiPeerType;
import im.actor.core.api.ApiOutPeer;
import im.actor.core.api.ApiUserOutPeer;
import im.actor.core.api.rpc.RequestGetReferencedEntitites;
import im.actor.core.entity.Group;
import im.actor.core.entity.Peer;
import im.actor.core.entity.PeerType;
import im.actor.core.entity.User;
import im.actor.core.modules.sequence.Updates;
import im.actor.core.network.RpcCallback;
import im.actor.core.network.RpcException;
import im.actor.core.network.parser.Request;
import im.actor.core.network.parser.Response;
import im.actor.core.viewmodel.GroupVM;
import im.actor.core.viewmodel.UserVM;
import im.actor.runtime.actors.AskcableActor;
import im.actor.runtime.actors.messages.Void;
import im.actor.runtime.function.Function;
import im.actor.runtime.promise.Promise;
import im.actor.runtime.promise.PromiseFunc;
import im.actor.runtime.promise.PromiseResolver;
import im.actor.runtime.eventbus.BusSubscriber;
import im.actor.runtime.eventbus.Event;
import im.actor.runtime.promise.Promises;
import im.actor.runtime.promise.PromisesArray;
import im.actor.runtime.storage.KeyValueEngine;
import im.actor.runtime.storage.PreferencesStorage;

public class ModuleActor extends AskcableActor implements BusSubscriber {

    protected static final long CURSOR_RECEIVED = 0;
    protected static final long CURSOR_READ = 1;
    protected static final long CURSOR_OWN_READ = 2;
    protected static final long CURSOR_DELETE = 3;

    private ModuleContext context;
    private BusSubscriber subscriber;

    public ModuleActor(ModuleContext context) {
        this.context = context;
    }

    public void subscribe(String eventType) {
        if (subscriber == null) {
            subscriber = event -> self().post(() -> ModuleActor.this.onBusEvent(event));
        }

        context().getEvents().subscribe(subscriber, eventType);
    }

    public ApiOutPeer buidOutPeer(Peer peer) {
        if (peer.getPeerType() == PeerType.PRIVATE) {
            User user = getUser(peer.getPeerId());
            if (user == null) {
                return null;
            }
            return new ApiOutPeer(ApiPeerType.PRIVATE, user.getUid(), user.getAccessHash());
        } else if (peer.getPeerType() == PeerType.GROUP) {
            Group group = getGroup(peer.getPeerId());
            if (group == null) {
                return null;
            }
            return new ApiOutPeer(ApiPeerType.GROUP, group.getGroupId(), group.getAccessHash());
        } else {
            throw new RuntimeException("Unknown peer: " + peer);
        }
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

    public <T extends Response> long request(Request<T> request) {
        return request(request, new RpcCallback<T>() {
            @Override
            public void onResult(T response) {

            }

            @Override
            public void onError(RpcException e) {

            }
        });
    }

    public <T extends Response> long request(final Request<T> request, final RpcCallback<T> callback) {
        return context.getActorApi().request(request, new RpcCallback<T>() {
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

    public <T extends Response> Promise<T> api(final Request<T> request) {
        return new Promise<>((PromiseFunc<T>) executor -> context.getActorApi().request(request, new RpcCallback<T>() {
            @Override
            public void onResult(T response) {
                executor.result(response);
            }

            @Override
            public void onError(RpcException e) {
                executor.error(e);
            }
        }));
    }

    public void cancelRequest(long rid) {
        context.getActorApi().cancelRequest(rid);
    }

    @Override
    public void onBusEvent(Event event) {

    }
}
