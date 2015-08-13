/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.core.modules.internal;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import im.actor.core.api.ApiGroup;
import im.actor.core.api.ApiPeer;
import im.actor.core.api.GroupOutPeer;
import im.actor.core.api.Member;
import im.actor.core.api.OutPeer;
import im.actor.core.api.ApiPeerType;
import im.actor.core.api.ServiceExUserJoined;
import im.actor.core.api.ServiceMessage;
import im.actor.core.api.UserOutPeer;
import im.actor.core.api.rpc.RequestCreateGroup;
import im.actor.core.api.rpc.RequestEditGroupTitle;
import im.actor.core.api.rpc.RequestEnterGroup;
import im.actor.core.api.rpc.RequestGetGroupInviteUrl;
import im.actor.core.api.rpc.RequestGetIntegrationToken;
import im.actor.core.api.rpc.RequestGetPublicGroups;
import im.actor.core.api.rpc.RequestInviteUser;
import im.actor.core.api.rpc.RequestJoinGroup;
import im.actor.core.api.rpc.RequestKickUser;
import im.actor.core.api.rpc.RequestLeaveGroup;
import im.actor.core.api.rpc.RequestRevokeIntegrationToken;
import im.actor.core.api.rpc.RequestRevokeInviteUrl;
import im.actor.core.api.rpc.ResponseCreateGroup;
import im.actor.core.api.rpc.ResponseEnterGroup;
import im.actor.core.api.rpc.ResponseGetPublicGroups;
import im.actor.core.api.rpc.ResponseIntegrationToken;
import im.actor.core.api.rpc.ResponseInviteUrl;
import im.actor.core.api.rpc.ResponseJoinGroup;
import im.actor.core.api.rpc.ResponseSeqDate;
import im.actor.core.api.updates.UpdateGroupInvite;
import im.actor.core.api.updates.UpdateGroupTitleChanged;
import im.actor.core.api.updates.UpdateGroupUserInvited;
import im.actor.core.api.updates.UpdateGroupUserKick;
import im.actor.core.api.updates.UpdateGroupUserLeave;
import im.actor.core.api.updates.UpdateMessage;
import im.actor.core.entity.Avatar;
import im.actor.core.entity.GroupEntity;
import im.actor.core.entity.PublicGroupEntity;
import im.actor.core.entity.UserEntity;
import im.actor.core.modules.AbsModule;
import im.actor.core.modules.ModuleContext;
import im.actor.core.modules.internal.avatar.GroupAvatarChangeActor;
import im.actor.core.modules.utils.RandomUtils;
import im.actor.core.network.RpcCallback;
import im.actor.core.network.RpcException;
import im.actor.core.network.RpcInternalException;
import im.actor.core.viewmodel.Command;
import im.actor.core.viewmodel.CommandCallback;
import im.actor.core.viewmodel.GroupAvatarVM;
import im.actor.core.viewmodel.GroupVM;
import im.actor.runtime.Storage;
import im.actor.runtime.actors.ActorCreator;
import im.actor.runtime.actors.ActorRef;
import im.actor.runtime.actors.Props;
import im.actor.runtime.mvvm.MVVMCollection;
import im.actor.runtime.storage.KeyValueEngine;

import static im.actor.runtime.actors.ActorSystem.system;

public class GroupsModule extends AbsModule {

    private KeyValueEngine<GroupEntity> groups;
    private MVVMCollection<GroupEntity, GroupVM> collection;
    private HashMap<Integer, GroupAvatarVM> avatarVMs;
    private ActorRef avatarChangeActor;

    public GroupsModule(final ModuleContext context) {
        super(context);
        
        collection = Storage.createKeyValue(STORAGE_GROUPS, GroupVM.CREATOR, GroupEntity.CREATOR);
        groups = collection.getEngine();

        avatarVMs = new HashMap<Integer, GroupAvatarVM>();
        avatarChangeActor = system().actorOf(Props.create(GroupAvatarChangeActor.class, new ActorCreator<GroupAvatarChangeActor>() {
            @Override
            public GroupAvatarChangeActor create() {
                return new GroupAvatarChangeActor(context);
            }
        }), "actor/avatar/group");
    }

    public GroupAvatarVM getAvatarVM(int gid) {
        synchronized (avatarVMs) {
            if (!avatarVMs.containsKey(gid)) {
                avatarVMs.put(gid, new GroupAvatarVM(gid));
            }
            return avatarVMs.get(gid);
        }
    }

    public KeyValueEngine<GroupEntity> getGroups() {
        return groups;
    }

    public MVVMCollection<GroupEntity, GroupVM> getGroupsCollection() {
        return collection;
    }

    public void changeAvatar(int gid, String descriptor) {
        avatarChangeActor.send(new GroupAvatarChangeActor.ChangeAvatar(gid, descriptor));
    }

    public void removeAvatar(int gid) {
        avatarChangeActor.send(new GroupAvatarChangeActor.RemoveAvatar(gid));
    }

    public Command<Integer> createGroup(final String title, final String avatarDescriptor, final int[] uids) {
        return new Command<Integer>() {
            @Override
            public void start(final CommandCallback<Integer> callback) {
                ArrayList<UserOutPeer> peers = new ArrayList<UserOutPeer>();
                for (int u : uids) {
                    UserEntity user = users().getValue(u);
                    if (user != null) {
                        peers.add(new UserOutPeer(u, user.getAccessHash()));
                    }
                }
                final long rid = RandomUtils.nextRid();
                request(new RequestCreateGroup(rid, title, peers), new RpcCallback<ResponseCreateGroup>() {
                    @Override
                    public void onResult(ResponseCreateGroup response) {
                        List<Member> members = new ArrayList<Member>();
                        for (int u : uids) {
                            members.add(new Member(u, myUid(), response.getDate(), u == myUid()));
                        }
                        final ApiGroup group = new ApiGroup(
                                response.getGroupPeer().getGroupId(),
                                response.getGroupPeer().getAccessHash(),
                                title, null, true, myUid(), members,
                                response.getDate(), null, null, null, null, null, true, null, null);
                        ArrayList<ApiGroup> groups = new ArrayList<ApiGroup>();
                        groups.add(group);

                        updates().onFatSeqUpdateReceived(
                                response.getSeq(),
                                response.getState(),
                                new UpdateGroupInvite(
                                        group.getId(),
                                        rid,
                                        myUid(),
                                        response.getDate()
                                ),
                                new ArrayList<im.actor.core.api.User>(),
                                groups);

                        updates().executeAfter(response.getSeq(), new Runnable() {
                            @Override
                            public void run() {
                                if (avatarDescriptor != null) {
                                    changeAvatar(group.getId(), avatarDescriptor);
                                }
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        callback.onResult(group.getId());
                                    }
                                });
                            }
                        });
                    }

                    @Override
                    public void onError(final RpcException e) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                callback.onError(e);
                            }
                        });
                    }
                });

            }
        };
    }

    public Command<Boolean> editTitle(final int gid, final String name) {
        return new Command<Boolean>() {
            @Override
            public void start(final CommandCallback<Boolean> callback) {
                GroupEntity group = getGroups().getValue(gid);
                if (group == null) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            callback.onError(new RpcInternalException());
                        }
                    });
                    return;
                }
                final long rid = RandomUtils.nextRid();
                request(new RequestEditGroupTitle(new GroupOutPeer(group.getGroupId(), group.getAccessHash()),
                        rid, name), new RpcCallback<ResponseSeqDate>() {
                    @Override
                    public void onResult(ResponseSeqDate response) {

                        updates().onSeqUpdateReceived(
                                response.getSeq(),
                                response.getState(),
                                new UpdateGroupTitleChanged(
                                        gid,
                                        rid,
                                        myUid(),
                                        name,
                                        response.getDate()));

                        updates().executeAfter(response.getSeq(), new Runnable() {
                            @Override
                            public void run() {
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        callback.onResult(true);
                                    }
                                });
                            }
                        });
                    }

                    @Override
                    public void onError(RpcException e) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                callback.onError(new RpcInternalException());
                            }
                        });
                    }
                });
            }
        };
    }

    public Command<Boolean> leaveGroup(final int gid) {
        return new Command<Boolean>() {
            @Override
            public void start(final CommandCallback<Boolean> callback) {
                GroupEntity group = getGroups().getValue(gid);
                if (group == null) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            callback.onError(new RpcInternalException());
                        }
                    });
                    return;
                }
                final long rid = RandomUtils.nextRid();
                request(new RequestLeaveGroup(new GroupOutPeer(group.getGroupId(), group.getAccessHash()),
                        rid), new RpcCallback<ResponseSeqDate>() {
                    @Override
                    public void onResult(ResponseSeqDate response) {

                        updates().onSeqUpdateReceived(
                                response.getSeq(),
                                response.getState(),
                                new UpdateGroupUserLeave(
                                        gid,
                                        rid,
                                        myUid(),
                                        response.getDate())
                        );

                        updates().executeAfter(response.getSeq(), new Runnable() {
                            @Override
                            public void run() {
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        callback.onError(new RpcInternalException());
                                    }
                                });
                            }
                        });

                    }

                    @Override
                    public void onError(RpcException e) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                callback.onError(new RpcInternalException());
                            }
                        });
                    }
                });
            }
        };
    }

    public Command<Boolean> addMemberToGroup(final int gid, final int uid) {
        return new Command<Boolean>() {
            @Override
            public void start(final CommandCallback<Boolean> callback) {
                GroupEntity group = getGroups().getValue(gid);
                UserEntity user = users().getValue(uid);
                if (group == null || user == null) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            callback.onError(new RpcInternalException());
                        }
                    });
                    return;
                }
                final long rid = RandomUtils.nextRid();
                request(new RequestInviteUser(new GroupOutPeer(group.getGroupId(), group.getAccessHash()),
                        rid, new UserOutPeer(uid, user.getAccessHash())), new RpcCallback<ResponseSeqDate>() {
                    @Override
                    public void onResult(ResponseSeqDate response) {
                        updates().onSeqUpdateReceived(
                                response.getSeq(),
                                response.getState(),
                                new UpdateGroupUserInvited(
                                        gid,
                                        rid,
                                        uid,
                                        myUid(),
                                        response.getDate()));

                        updates().executeAfter(response.getSeq(), new Runnable() {
                            @Override
                            public void run() {
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        callback.onResult(true);
                                    }
                                });
                            }
                        });
                    }

                    @Override
                    public void onError(RpcException e) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                callback.onError(new RpcInternalException());
                            }
                        });
                    }
                });
            }
        };
    }

    public Command<Boolean> kickMember(final int gid, final int uid) {
        return new Command<Boolean>() {
            @Override
            public void start(final CommandCallback<Boolean> callback) {
                GroupEntity group = getGroups().getValue(gid);
                UserEntity user = users().getValue(uid);
                if (group == null || user == null) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            callback.onError(new RpcInternalException());
                        }
                    });
                    return;
                }
                final long rid = RandomUtils.nextRid();
                request(new RequestKickUser(new GroupOutPeer(group.getGroupId(), group.getAccessHash()),
                        rid, new UserOutPeer(uid, user.getAccessHash())), new RpcCallback<ResponseSeqDate>() {
                    @Override
                    public void onResult(ResponseSeqDate response) {

                        updates().onSeqUpdateReceived(
                                response.getSeq(),
                                response.getState(),
                                new UpdateGroupUserKick(
                                        gid,
                                        rid,
                                        uid,
                                        myUid(),
                                        response.getDate()));

                        updates().executeAfter(response.getSeq(), new Runnable() {
                            @Override
                            public void run() {
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        callback.onResult(true);
                                    }
                                });
                            }
                        });

                    }

                    @Override
                    public void onError(RpcException e) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                callback.onError(new RpcInternalException());
                            }
                        });
                    }
                });
            }
        };
    }

    public Command<String> requestInviteLink(final int gid) {
        return new Command<String>() {
            @Override
            public void start(final CommandCallback<String> callback) {
                final GroupEntity group = getGroups().getValue(gid);
                if (group == null) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            callback.onError(new RpcInternalException());
                        }
                    });
                    return;
                }
                request(new RequestGetGroupInviteUrl(new GroupOutPeer(group.getGroupId(), group.getAccessHash())), new RpcCallback<ResponseInviteUrl>() {
                    @Override
                    public void onResult(final ResponseInviteUrl response) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                callback.onResult(response.getUrl());
                            }
                        });
                    }

                    @Override
                    public void onError(RpcException e) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                callback.onError(new RpcInternalException());
                            }
                        });
                    }
                });
            }
        };
    }

    public Command<String> requestRevokeLink(final int gid) {
        return new Command<String>() {
            @Override
            public void start(final CommandCallback<String> callback) {
                final GroupEntity group = getGroups().getValue(gid);
                if (group == null) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            callback.onError(new RpcInternalException());
                        }
                    });
                    return;
                }
                request(new RequestRevokeInviteUrl(new GroupOutPeer(group.getGroupId(), group.getAccessHash())), new RpcCallback<ResponseInviteUrl>() {
                    @Override
                    public void onResult(final ResponseInviteUrl response) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                callback.onResult(response.getUrl());
                            }
                        });
                    }

                    @Override
                    public void onError(RpcException e) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                callback.onError(new RpcInternalException());
                            }
                        });
                    }
                });
            }
        };
    }

    public Command<Integer> joinGroupViaLink(final String url) {
        return new Command<Integer>() {
            @Override
            public void start(final CommandCallback<Integer> callback) {
                request(new RequestJoinGroup(url), new RpcCallback<ResponseJoinGroup>() {
                            @Override
                            public void onResult(final ResponseJoinGroup response) {

                                ApiGroup group = response.getGroup();
                                ArrayList<ApiGroup> groups = new ArrayList<ApiGroup>();
                                groups.add(group);

                                updates().onFatSeqUpdateReceived(
                                        response.getSeq(),
                                        response.getState(),
                                        new UpdateMessage(
                                                new ApiPeer(ApiPeerType.GROUP, group.getId()),
                                                myUid(),
                                                response.getDate(),
                                                response.getRid(),
                                                new ServiceMessage("Joined chat",
                                                        new ServiceExUserJoined())),
                                        response.getUsers(),
                                        groups);

                                updates().executeAfter(response.getSeq(), new Runnable() {
                                    @Override
                                    public void run() {
                                        runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                callback.onResult(response.getGroup().getId());
                                            }
                                        });
                                    }
                                });
                            }

                            @Override
                            public void onError(final RpcException e) {
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        callback.onError(e);
                                    }
                                });
                            }
                        }

                );
            }
        };
    }

    public Command<String> requestIntegrationToken(final int gid) {
        return new Command<String>() {
            @Override
            public void start(final CommandCallback<String> callback) {
                final GroupEntity group = getGroups().getValue(gid);
                if (group == null) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            callback.onError(new RpcInternalException());
                        }
                    });
                    return;
                }
                request(new RequestGetIntegrationToken(new OutPeer(ApiPeerType.GROUP, group.getGroupId(), group.getAccessHash())), new RpcCallback<ResponseIntegrationToken>() {
                    @Override
                    public void onResult(final ResponseIntegrationToken response) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                callback.onResult(response.getUrl());
                            }
                        });
                    }

                    @Override
                    public void onError(RpcException e) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                callback.onError(new RpcInternalException());
                            }
                        });
                    }
                });
            }
        };
    }

    public Command<String> revokeIntegrationToken(final int gid) {
        return new Command<String>() {
            @Override
            public void start(final CommandCallback<String> callback) {
                final GroupEntity group = getGroups().getValue(gid);
                if (group == null) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            callback.onError(new RpcInternalException());
                        }
                    });
                    return;
                }
                request(new RequestRevokeIntegrationToken(new OutPeer(ApiPeerType.GROUP, group.getGroupId(), group.getAccessHash())), new RpcCallback<ResponseIntegrationToken>() {
                    @Override
                    public void onResult(final ResponseIntegrationToken response) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                callback.onResult(response.getUrl());
                            }
                        });
                    }

                    @Override
                    public void onError(RpcException e) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                callback.onError(new RpcInternalException());
                            }
                        });
                    }
                });
            }
        };
    }

    public Command<Integer> joinPublicGroup(final int gid, final long accessHash) {
        return new Command<Integer>() {
            @Override
            public void start(final CommandCallback<Integer> callback) {
                request(new RequestEnterGroup(new GroupOutPeer(gid, accessHash)), new RpcCallback<ResponseEnterGroup>() {
                    @Override
                    public void onResult(final ResponseEnterGroup response) {
                        ApiGroup group = response.getGroup();
                        ArrayList<ApiGroup> groups = new ArrayList<ApiGroup>();
                        groups.add(group);

                        updates().onFatSeqUpdateReceived(
                                response.getSeq(),
                                response.getState(),
                                new UpdateMessage(
                                        new ApiPeer(ApiPeerType.GROUP, group.getId()),
                                        myUid(),
                                        response.getDate(),
                                        response.getRid(),
                                        new ServiceMessage("Joined chat",
                                                new ServiceExUserJoined())),
                                response.getUsers(),
                                groups);

                        updates().executeAfter(response.getSeq(), new Runnable() {
                            @Override
                            public void run() {
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        callback.onResult(response.getGroup().getId());
                                    }
                                });
                            }
                        });
                    }

                    @Override
                    public void onError(final RpcException e) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                callback.onError(e);
                            }
                        });
                    }
                });
            }
        };
    }

    public Command<List<PublicGroupEntity>> listPublicGroups() {
        return new Command<List<PublicGroupEntity>>() {
            @Override
            public void start(final CommandCallback<List<PublicGroupEntity>> callback) {
                request(new RequestGetPublicGroups(), new RpcCallback<ResponseGetPublicGroups>() {
                    @Override
                    public void onResult(ResponseGetPublicGroups response) {
                        ArrayList<PublicGroupEntity> groups = new ArrayList<PublicGroupEntity>();
                        for (im.actor.core.api.PublicGroup g : response.getGroups()) {
                            Avatar avatar = null;
                            if (g.getAvatar() != null) {
                                avatar = new Avatar(g.getAvatar());
                            }
                            groups.add(new PublicGroupEntity(g.getId(), g.getAccessHash(),
                                    g.getTitle(), avatar, g.getDescription(), g.getMembersCount(), g.getFriendsCount()));
                        }
                        callback.onResult(groups);
                    }

                    @Override
                    public void onError(final RpcException e) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                callback.onError(e);
                            }
                        });
                    }
                });
            }
        };
    }

    public void resetModule() {
        groups.clear();
    }
}