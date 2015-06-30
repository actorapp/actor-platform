/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.model.modules;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import im.actor.model.api.GroupOutPeer;
import im.actor.model.api.Member;
import im.actor.model.api.OutPeer;
import im.actor.model.api.PeerType;
import im.actor.model.api.ServiceExUserJoined;
import im.actor.model.api.ServiceMessage;
import im.actor.model.api.UserOutPeer;
import im.actor.model.api.rpc.RequestCreateGroup;
import im.actor.model.api.rpc.RequestEditGroupTitle;
import im.actor.model.api.rpc.RequestGetGroupInviteUrl;
import im.actor.model.api.rpc.RequestGetIntegrationToken;
import im.actor.model.api.rpc.RequestGetPublicGroups;
import im.actor.model.api.rpc.RequestInviteUser;
import im.actor.model.api.rpc.RequestJoinGroup;
import im.actor.model.api.rpc.RequestJoinGroupDirect;
import im.actor.model.api.rpc.RequestKickUser;
import im.actor.model.api.rpc.RequestLeaveGroup;
import im.actor.model.api.rpc.RequestRevokeIntegrationToken;
import im.actor.model.api.rpc.RequestRevokeInviteUrl;
import im.actor.model.api.rpc.ResponseCreateGroup;
import im.actor.model.api.rpc.ResponseGetPublicGroups;
import im.actor.model.api.rpc.ResponseIntegrationToken;
import im.actor.model.api.rpc.ResponseInviteUrl;
import im.actor.model.api.rpc.ResponseJoinGroup;
import im.actor.model.api.rpc.ResponseJoinGroupDirect;
import im.actor.model.api.rpc.ResponseSeqDate;
import im.actor.model.api.updates.UpdateGroupInvite;
import im.actor.model.api.updates.UpdateGroupTitleChanged;
import im.actor.model.api.updates.UpdateGroupUserInvited;
import im.actor.model.api.updates.UpdateGroupUserKick;
import im.actor.model.api.updates.UpdateGroupUserLeave;
import im.actor.model.api.updates.UpdateMessage;
import im.actor.model.concurrency.Command;
import im.actor.model.concurrency.CommandCallback;
import im.actor.model.droidkit.actors.ActorCreator;
import im.actor.model.droidkit.actors.ActorRef;
import im.actor.model.droidkit.actors.Props;
import im.actor.model.droidkit.engine.KeyValueEngine;
import im.actor.model.entity.Avatar;
import im.actor.model.entity.Group;
import im.actor.model.entity.PublicGroup;
import im.actor.model.entity.User;
import im.actor.model.modules.avatar.GroupAvatarChangeActor;
import im.actor.model.modules.utils.RandomUtils;
import im.actor.model.mvvm.MVVMCollection;
import im.actor.model.network.RpcCallback;
import im.actor.model.network.RpcException;
import im.actor.model.network.RpcInternalException;
import im.actor.model.viewmodel.GroupAvatarVM;
import im.actor.model.viewmodel.GroupVM;

import static im.actor.model.droidkit.actors.ActorSystem.system;

public class Groups extends BaseModule {

    private KeyValueEngine<Group> groups;
    private MVVMCollection<Group, GroupVM> collection;
    private HashMap<Integer, GroupAvatarVM> avatarVMs;
    private ActorRef avatarChangeActor;

    public Groups(final Modules modules) {
        super(modules);
        collection = new MVVMCollection<Group, GroupVM>(modules.getConfiguration().getStorageProvider().createKeyValue(STORAGE_GROUPS)) {
            @Override
            protected GroupVM createNew(Group raw) {
                return new GroupVM(raw);
            }

            @Override
            protected byte[] serialize(Group raw) {
                return raw.toByteArray();
            }

            @Override
            protected Group deserialize(byte[] raw) {
                try {
                    return new Group(raw);
                } catch (IOException e) {
                    e.printStackTrace();
                    return null;
                }
            }
        };
        groups = collection.getEngine();
        avatarVMs = new HashMap<Integer, GroupAvatarVM>();
        avatarChangeActor = system().actorOf(Props.create(GroupAvatarChangeActor.class, new ActorCreator<GroupAvatarChangeActor>() {
            @Override
            public GroupAvatarChangeActor create() {
                return new GroupAvatarChangeActor(modules);
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

    public KeyValueEngine<Group> getGroups() {
        return groups;
    }

    public MVVMCollection<Group, GroupVM> getGroupsCollection() {
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
                    User user = users().getValue(u);
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
                            members.add(new Member(u, myUid(), response.getDate()));
                        }
                        final im.actor.model.api.Group group = new im.actor.model.api.Group(
                                response.getGroupPeer().getGroupId(),
                                response.getGroupPeer().getAccessHash(),
                                title, null, true, myUid(), members,
                                response.getDate(), null, null, null, null, null, true);
                        ArrayList<im.actor.model.api.Group> groups = new ArrayList<im.actor.model.api.Group>();
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
                                new ArrayList<im.actor.model.api.User>(),
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
                Group group = getGroups().getValue(gid);
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
                Group group = getGroups().getValue(gid);
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
                Group group = getGroups().getValue(gid);
                User user = users().getValue(uid);
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
                Group group = getGroups().getValue(gid);
                User user = users().getValue(uid);
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
                final Group group = getGroups().getValue(gid);
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
                final Group group = getGroups().getValue(gid);
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

                                im.actor.model.api.Group group = response.getGroup();
                                ArrayList<im.actor.model.api.Group> groups = new ArrayList<im.actor.model.api.Group>();
                                groups.add(group);

                                updates().onFatSeqUpdateReceived(
                                        response.getSeq(),
                                        response.getState(),
                                        new UpdateMessage(
                                                new im.actor.model.api.Peer(PeerType.GROUP, group.getId()),
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
                            public void onError(RpcException e) {
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        callback.onError(new RpcInternalException());
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
                final Group group = getGroups().getValue(gid);
                if (group == null) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            callback.onError(new RpcInternalException());
                        }
                    });
                    return;
                }
                request(new RequestGetIntegrationToken(new OutPeer(PeerType.GROUP, group.getGroupId(), group.getAccessHash())), new RpcCallback<ResponseIntegrationToken>() {
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
                final Group group = getGroups().getValue(gid);
                if (group == null) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            callback.onError(new RpcInternalException());
                        }
                    });
                    return;
                }
                request(new RequestRevokeIntegrationToken(new OutPeer(PeerType.GROUP, group.getGroupId(), group.getAccessHash())), new RpcCallback<ResponseIntegrationToken>() {
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
                request(new RequestJoinGroupDirect(new GroupOutPeer(gid, accessHash)), new RpcCallback<ResponseJoinGroupDirect>() {
                    @Override
                    public void onResult(final ResponseJoinGroupDirect response) {
                        im.actor.model.api.Group group = response.getGroup();
                        ArrayList<im.actor.model.api.Group> groups = new ArrayList<im.actor.model.api.Group>();
                        groups.add(group);

                        updates().onFatSeqUpdateReceived(
                                response.getSeq(),
                                response.getState(),
                                new UpdateMessage(
                                        new im.actor.model.api.Peer(PeerType.GROUP, group.getId()),
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

    public Command<List<PublicGroup>> listPublicGroups() {
        return new Command<List<PublicGroup>>() {
            @Override
            public void start(final CommandCallback<List<PublicGroup>> callback) {
                request(new RequestGetPublicGroups(), new RpcCallback<ResponseGetPublicGroups>() {
                    @Override
                    public void onResult(ResponseGetPublicGroups response) {
                        ArrayList<PublicGroup> groups = new ArrayList<PublicGroup>();
                        for (im.actor.model.api.PublicGroup g : response.getGroups()) {
                            Avatar avatar = null;
                            if (g.getAvatar() != null) {
                                avatar = new Avatar(g.getAvatar());
                            }
                            groups.add(new PublicGroup(g.getId(), g.getAccessHash(),
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