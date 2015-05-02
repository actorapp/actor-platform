/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.model.modules;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import im.actor.model.api.Email;
import im.actor.model.api.GroupOutPeer;
import im.actor.model.api.Member;
import im.actor.model.api.Phone;
import im.actor.model.api.UserOutPeer;
import im.actor.model.api.base.FatSeqUpdate;
import im.actor.model.api.base.SeqUpdate;
import im.actor.model.api.rpc.RequestCreateGroup;
import im.actor.model.api.rpc.RequestEditGroupTitle;
import im.actor.model.api.rpc.RequestInviteUser;
import im.actor.model.api.rpc.RequestKickUser;
import im.actor.model.api.rpc.RequestLeaveGroup;
import im.actor.model.api.rpc.ResponseCreateGroup;
import im.actor.model.api.rpc.ResponseSeqDate;
import im.actor.model.api.updates.UpdateGroupInvite;
import im.actor.model.api.updates.UpdateGroupTitleChanged;
import im.actor.model.api.updates.UpdateGroupUserAdded;
import im.actor.model.api.updates.UpdateGroupUserKick;
import im.actor.model.api.updates.UpdateGroupUserLeave;
import im.actor.model.concurrency.Command;
import im.actor.model.concurrency.CommandCallback;
import im.actor.model.droidkit.actors.ActorCreator;
import im.actor.model.droidkit.actors.ActorRef;
import im.actor.model.droidkit.actors.Props;
import im.actor.model.droidkit.engine.KeyValueEngine;
import im.actor.model.entity.Group;
import im.actor.model.entity.User;
import im.actor.model.modules.avatar.GroupAvatarChangeActor;
import im.actor.model.modules.updates.internal.GroupCreated;
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
                    return Group.fromBytes(raw);
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
                        im.actor.model.api.Group group = new im.actor.model.api.Group(
                                response.getGroupPeer().getGroupId(),
                                response.getGroupPeer().getAccessHash(),
                                title, null, true, myUid(), members,
                                response.getDate());
                        ArrayList<im.actor.model.api.Group> groups = new ArrayList<im.actor.model.api.Group>();
                        groups.add(group);

                        if (avatarDescriptor != null) {
                            changeAvatar(group.getId(), avatarDescriptor);
                        }

                        updates().onUpdateReceived(new FatSeqUpdate(response.getSeq(),
                                response.getState(),
                                UpdateGroupInvite.HEADER,
                                new UpdateGroupInvite(response.getGroupPeer().getGroupId(),
                                        rid, myUid(), response.getDate()).toByteArray(),
                                new ArrayList<im.actor.model.api.User>(), groups, new ArrayList<Phone>(), new ArrayList<Email>()));
                        updates().onUpdateReceived(new GroupCreated(group, callback));
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
                        SeqUpdate update = new SeqUpdate(response.getSeq(), response.getState(),
                                UpdateGroupTitleChanged.HEADER,
                                new UpdateGroupTitleChanged(gid, rid, myUid(),
                                        name, response.getDate()).toByteArray());
                        updates().onUpdateReceived(update);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                callback.onResult(true);
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
                        SeqUpdate update = new SeqUpdate(response.getSeq(), response.getState(),
                                UpdateGroupUserLeave.HEADER,
                                new UpdateGroupUserLeave(gid, rid, myUid(),
                                        response.getDate()).toByteArray());
                        updates().onUpdateReceived(update);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                callback.onResult(true);
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
                        SeqUpdate update = new SeqUpdate(response.getSeq(), response.getState(),
                                UpdateGroupUserAdded.HEADER,
                                new UpdateGroupUserAdded(gid, rid, uid, myUid(),
                                        response.getDate()).toByteArray());
                        updates().onUpdateReceived(update);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                callback.onResult(true);
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
                        SeqUpdate update = new SeqUpdate(response.getSeq(), response.getState(),
                                UpdateGroupUserKick.HEADER,
                                new UpdateGroupUserKick(gid, rid, uid, myUid(),
                                        response.getDate()).toByteArray());
                        updates().onUpdateReceived(update);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                callback.onResult(true);
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
}