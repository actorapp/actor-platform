/*
 * Copyright (C) 2014-2015 Actor LLC. <https://actor.im>
 */

package im.actor.core.modules.groups;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import im.actor.core.api.ApiGroup;
import im.actor.core.api.ApiGroupOutPeer;
import im.actor.core.api.ApiMember;
import im.actor.core.api.ApiMessageAttributes;
import im.actor.core.api.ApiOutPeer;
import im.actor.core.api.ApiPeer;
import im.actor.core.api.ApiPeerType;
import im.actor.core.api.ApiPublicGroup;
import im.actor.core.api.ApiServiceExUserJoined;
import im.actor.core.api.ApiServiceMessage;
import im.actor.core.api.ApiUser;
import im.actor.core.api.ApiUserOutPeer;
import im.actor.core.api.rpc.RequestCreateGroupObsolete;
import im.actor.core.api.rpc.RequestEditGroupAbout;
import im.actor.core.api.rpc.RequestEditGroupTitle;
import im.actor.core.api.rpc.RequestEditGroupTopic;
import im.actor.core.api.rpc.RequestEnterGroup;
import im.actor.core.api.rpc.RequestGetGroupInviteUrl;
import im.actor.core.api.rpc.RequestGetIntegrationToken;
import im.actor.core.api.rpc.RequestGetPublicGroups;
import im.actor.core.api.rpc.RequestInviteUser;
import im.actor.core.api.rpc.RequestJoinGroup;
import im.actor.core.api.rpc.RequestKickUser;
import im.actor.core.api.rpc.RequestLeaveGroup;
import im.actor.core.api.rpc.RequestMakeUserAdmin;
import im.actor.core.api.rpc.RequestRevokeIntegrationToken;
import im.actor.core.api.rpc.RequestRevokeInviteUrl;
import im.actor.core.api.rpc.ResponseCreateGroupObsolete;
import im.actor.core.api.rpc.ResponseEnterGroup;
import im.actor.core.api.rpc.ResponseGetPublicGroups;
import im.actor.core.api.rpc.ResponseIntegrationToken;
import im.actor.core.api.rpc.ResponseInviteUrl;
import im.actor.core.api.rpc.ResponseJoinGroup;
import im.actor.core.api.rpc.ResponseMakeUserAdmin;
import im.actor.core.api.rpc.ResponseSeqDate;
import im.actor.core.api.updates.UpdateGroupAboutChanged;
import im.actor.core.api.updates.UpdateGroupInvite;
import im.actor.core.api.updates.UpdateGroupMembersUpdate;
import im.actor.core.api.updates.UpdateGroupTitleChanged;
import im.actor.core.api.updates.UpdateGroupTopicChanged;
import im.actor.core.api.updates.UpdateGroupUserInvited;
import im.actor.core.api.updates.UpdateGroupUserKick;
import im.actor.core.api.updates.UpdateGroupUserLeave;
import im.actor.core.api.updates.UpdateMessage;
import im.actor.core.entity.Avatar;
import im.actor.core.entity.Group;
import im.actor.core.entity.PublicGroup;
import im.actor.core.entity.User;
import im.actor.core.modules.AbsModule;
import im.actor.core.modules.ModuleContext;
import im.actor.core.modules.profile.avatar.GroupAvatarChangeActor;
import im.actor.core.util.RandomUtils;
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

    private KeyValueEngine<Group> groups;
    private MVVMCollection<Group, GroupVM> collection;
    private HashMap<Integer, GroupAvatarVM> avatarVMs;
    private ActorRef avatarChangeActor;

    public GroupsModule(final ModuleContext context) {
        super(context);

        collection = Storage.createKeyValue(STORAGE_GROUPS, GroupVM.CREATOR(context.getAuthModule().myUid()), Group.CREATOR);
        groups = collection.getEngine();

        avatarVMs = new HashMap<>();
        avatarChangeActor = system().actorOf(Props.create(new ActorCreator() {
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
        return callback -> {
            ArrayList<ApiUserOutPeer> peers = new ArrayList<>();
            for (int u : uids) {
                User user = users().getValue(u);
                if (user != null) {
                    peers.add(new ApiUserOutPeer(u, user.getAccessHash()));
                }
            }
            final long rid = RandomUtils.nextRid();
            request(new RequestCreateGroupObsolete(rid, title, peers), new RpcCallback<ResponseCreateGroupObsolete>() {
                @Override
                public void onResult(ResponseCreateGroupObsolete response) {
                    List<ApiMember> members = new ArrayList<>();
                    for (int u : response.getUsers()) {
                        members.add(new ApiMember(u, myUid(), response.getDate(), u == myUid()));
                    }
                    final ApiGroup group = new ApiGroup(
                            response.getGroupPeer().getGroupId(),
                            response.getGroupPeer().getAccessHash(),
                            title, null, myUid(), members,
                            response.getDate(), null,
                            null, false, null, null);
                    ArrayList<ApiGroup> groups1 = new ArrayList<>();
                    groups1.add(group);

                    updates().onFatSeqUpdateReceived(
                            response.getSeq(),
                            response.getState(),
                            new UpdateGroupInvite(
                                    group.getId(),
                                    rid,
                                    myUid(),
                                    response.getDate()
                            ),
                            new ArrayList<>(),
                            groups1);

                    updates().executeAfter(response.getSeq(), () -> {
                        if (avatarDescriptor != null) {
                            changeAvatar(group.getId(), avatarDescriptor);
                        }
                        runOnUiThread(() -> callback.onResult(group.getId()));
                    });
                }

                @Override
                public void onError(final RpcException e) {
                    runOnUiThread(() -> callback.onError(e));
                }
            }, RPC_TIMEOUT);

        };
    }

    public Command<Boolean> editTitle(final int gid, final String name) {
        return callback -> {
            Group group = getGroups().getValue(gid);
            if (group == null) {
                runOnUiThread(() -> callback.onError(new RpcInternalException()));
                return;
            }
            final long rid = RandomUtils.nextRid();
            request(new RequestEditGroupTitle(new ApiGroupOutPeer(group.getGroupId(), group.getAccessHash()),
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

                    updates().executeAfter(response.getSeq(), () -> runOnUiThread(() -> callback.onResult(true)));
                }

                @Override
                public void onError(final RpcException e) {
                    runOnUiThread(() -> callback.onError(e));
                }
            }, RPC_TIMEOUT);
        };
    }

    public Command<Boolean> editTheme(final int gid, final String theme) {
        return callback -> {
            Group group = getGroups().getValue(gid);
            if (group == null) {
                runOnUiThread(() -> callback.onError(new RpcInternalException()));
                return;
            }
            final long rid = RandomUtils.nextRid();
            request(new RequestEditGroupTopic(new ApiGroupOutPeer(group.getGroupId(), group.getAccessHash()),
                    rid, theme), new RpcCallback<ResponseSeqDate>() {
                @Override
                public void onResult(ResponseSeqDate response) {

                    updates().onSeqUpdateReceived(
                            response.getSeq(),
                            response.getState(),
                            new UpdateGroupTopicChanged(
                                    gid,
                                    rid,
                                    myUid(),
                                    theme,
                                    response.getDate()));

                    updates().executeAfter(response.getSeq(), () -> runOnUiThread(() -> callback.onResult(true)));
                }

                @Override
                public void onError(final RpcException e) {
                    runOnUiThread(() -> callback.onError(e));
                }
            }, RPC_TIMEOUT);
        };
    }

    public Command<Boolean> editAbout(final int gid, final String about) {
        return callback -> {
            Group group = getGroups().getValue(gid);
            if (group == null) {
                runOnUiThread(() -> callback.onError(new RpcInternalException()));
                return;
            }
            final long rid = RandomUtils.nextRid();
            request(new RequestEditGroupAbout(new ApiGroupOutPeer(group.getGroupId(), group.getAccessHash()),
                    rid, about), new RpcCallback<ResponseSeqDate>() {
                @Override
                public void onResult(ResponseSeqDate response) {

                    updates().onSeqUpdateReceived(
                            response.getSeq(),
                            response.getState(),
                            new UpdateGroupAboutChanged(
                                    gid,
                                    about));

                    updates().executeAfter(response.getSeq(), () -> runOnUiThread(() -> callback.onResult(true)));
                }

                @Override
                public void onError(final RpcException e) {
                    runOnUiThread(() -> callback.onError(e));
                }
            }, RPC_TIMEOUT);
        };
    }

    public Command<Boolean> leaveGroup(final int gid) {
        return callback -> {
            Group group = getGroups().getValue(gid);
            if (group == null) {
                runOnUiThread(() -> callback.onError(new RpcInternalException()));
                return;
            }
            final long rid = RandomUtils.nextRid();
            request(new RequestLeaveGroup(new ApiGroupOutPeer(group.getGroupId(), group.getAccessHash()),
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

                    updates().executeAfter(response.getSeq(), () -> runOnUiThread(() -> callback.onResult(true)));

                }

                @Override
                public void onError(final RpcException e) {
                    runOnUiThread(() -> callback.onError(e));
                }
            }, RPC_TIMEOUT);
        };
    }

    public Command<Boolean> addMemberToGroup(final int gid, final int uid) {
        return callback -> {
            Group group = getGroups().getValue(gid);
            User user = users().getValue(uid);
            if (group == null || user == null) {
                runOnUiThread(() -> callback.onError(new RpcInternalException()));
                return;
            }
            final long rid = RandomUtils.nextRid();
            request(new RequestInviteUser(new ApiGroupOutPeer(group.getGroupId(), group.getAccessHash()),
                    rid, new ApiUserOutPeer(uid, user.getAccessHash())), new RpcCallback<ResponseSeqDate>() {
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

                    updates().executeAfter(response.getSeq(), () -> runOnUiThread(() -> callback.onResult(true)));
                }

                @Override
                public void onError(final RpcException e) {
                    runOnUiThread(() -> callback.onError(e));
                }
            }, RPC_TIMEOUT);
        };
    }

    public Command<Boolean> makeAdmin(final int gid, final int uid) {
        return callback -> {
            Group group = getGroups().getValue(gid);
            User user = users().getValue(uid);
            if (group == null || user == null) {
                runOnUiThread(() -> callback.onError(new RpcInternalException()));
                return;
            }

            request(new RequestMakeUserAdmin(new ApiGroupOutPeer(group.getGroupId(), group.getAccessHash()),
                    new ApiUserOutPeer(uid, user.getAccessHash())), new RpcCallback<ResponseMakeUserAdmin>() {
                @Override
                public void onResult(ResponseMakeUserAdmin response) {
                    updates().onSeqUpdateReceived(
                            response.getSeq(),
                            response.getState(),
                            new UpdateGroupMembersUpdate(gid, response.getMembers()));

                    updates().executeAfter(response.getSeq(), () -> runOnUiThread(() -> callback.onResult(true)));
                }

                @Override
                public void onError(final RpcException e) {
                    runOnUiThread(() -> callback.onError(e));
                }
            }, RPC_TIMEOUT);
        };
    }


    public Command<Boolean> kickMember(final int gid, final int uid) {
        return callback -> {
            Group group = getGroups().getValue(gid);
            User user = users().getValue(uid);
            if (group == null || user == null) {
                runOnUiThread(() -> callback.onError(new RpcInternalException()));
                return;
            }
            final long rid = RandomUtils.nextRid();
            request(new RequestKickUser(new ApiGroupOutPeer(group.getGroupId(), group.getAccessHash()),
                    rid, new ApiUserOutPeer(uid, user.getAccessHash())), new RpcCallback<ResponseSeqDate>() {
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

                    updates().executeAfter(response.getSeq(), () -> runOnUiThread(() -> callback.onResult(true)));

                }

                @Override
                public void onError(final RpcException e) {
                    runOnUiThread(() -> callback.onError(e));
                }
            }, RPC_TIMEOUT);
        };
    }

    public Command<String> requestInviteLink(final int gid) {
        return callback -> {
            final Group group = getGroups().getValue(gid);
            if (group == null) {
                runOnUiThread(() -> callback.onError(new RpcInternalException()));
                return;
            }
            request(new RequestGetGroupInviteUrl(new ApiGroupOutPeer(group.getGroupId(), group.getAccessHash())), new RpcCallback<ResponseInviteUrl>() {
                @Override
                public void onResult(final ResponseInviteUrl response) {
                    runOnUiThread(() -> callback.onResult(response.getUrl()));
                }

                @Override
                public void onError(final RpcException e) {
                    runOnUiThread(() -> callback.onError(e));
                }
            });
        };
    }

    public Command<String> requestRevokeLink(final int gid) {
        return callback -> {
            final Group group = getGroups().getValue(gid);
            if (group == null) {
                runOnUiThread(() -> callback.onError(new RpcInternalException()));
                return;
            }
            request(new RequestRevokeInviteUrl(new ApiGroupOutPeer(group.getGroupId(), group.getAccessHash())), new RpcCallback<ResponseInviteUrl>() {
                @Override
                public void onResult(final ResponseInviteUrl response) {
                    runOnUiThread(() -> callback.onResult(response.getUrl()));
                }

                @Override
                public void onError(final RpcException e) {
                    runOnUiThread(() -> callback.onError(e));
                }
            }, RPC_TIMEOUT);
        };
    }

    public Command<Integer> joinGroupViaLink(final String url) {
        return callback -> request(new RequestJoinGroup(url), new RpcCallback<ResponseJoinGroup>() {
                    @Override
                    public void onResult(final ResponseJoinGroup response) {

                        ApiGroup group = response.getGroup();
                        ArrayList<ApiGroup> groups1 = new ArrayList<>();
                        groups1.add(group);

                        updates().onFatSeqUpdateReceived(
                                response.getSeq(),
                                response.getState(),
                                new UpdateMessage(
                                        new ApiPeer(ApiPeerType.GROUP, group.getId()),
                                        myUid(),
                                        response.getDate(),
                                        response.getRid(),
                                        new ApiServiceMessage("Joined chat",
                                                new ApiServiceExUserJoined()),
                                        new ApiMessageAttributes()),
                                response.getUsers(),
                                groups1);

                        updates().executeAfter(response.getSeq(), () -> runOnUiThread(() -> callback.onResult(response.getGroup().getId())));
                    }

                    @Override
                    public void onError(final RpcException e) {
                        runOnUiThread(() -> callback.onError(e));
                    }
                }

        );
    }

    public Command<String> requestIntegrationToken(final int gid) {
        return callback -> {
            final Group group = getGroups().getValue(gid);
            if (group == null) {
                runOnUiThread(() -> callback.onError(new RpcInternalException()));
                return;
            }
            request(new RequestGetIntegrationToken(new ApiOutPeer(ApiPeerType.GROUP, group.getGroupId(), group.getAccessHash())), new RpcCallback<ResponseIntegrationToken>() {
                @Override
                public void onResult(final ResponseIntegrationToken response) {
                    runOnUiThread(() -> callback.onResult(response.getUrl()));
                }

                @Override
                public void onError(final RpcException e) {
                    runOnUiThread(() -> callback.onError(e));
                }
            }, RPC_TIMEOUT);
        };
    }

    public Command<String> revokeIntegrationToken(final int gid) {
        return callback -> {
            final Group group = getGroups().getValue(gid);
            if (group == null) {
                runOnUiThread(() -> callback.onError(new RpcInternalException()));
                return;
            }
            request(new RequestRevokeIntegrationToken(new ApiOutPeer(ApiPeerType.GROUP, group.getGroupId(), group.getAccessHash())), new RpcCallback<ResponseIntegrationToken>() {
                @Override
                public void onResult(final ResponseIntegrationToken response) {
                    runOnUiThread(() -> callback.onResult(response.getUrl()));
                }

                @Override
                public void onError(final RpcException e) {
                    runOnUiThread(() -> callback.onError(e));
                }
            }, RPC_TIMEOUT);
        };
    }

    public Command<Integer> joinPublicGroup(final int gid, final long accessHash) {
        return callback -> request(new RequestEnterGroup(new ApiGroupOutPeer(gid, accessHash)), new RpcCallback<ResponseEnterGroup>() {
            @Override
            public void onResult(final ResponseEnterGroup response) {
                ApiGroup group = response.getGroup();
                ArrayList<ApiGroup> groups1 = new ArrayList<>();
                groups1.add(group);

                updates().onFatSeqUpdateReceived(
                        response.getSeq(),
                        response.getState(),
                        new UpdateMessage(
                                new ApiPeer(ApiPeerType.GROUP, group.getId()),
                                myUid(),
                                response.getDate(),
                                response.getRid(),
                                new ApiServiceMessage("Joined chat",
                                        new ApiServiceExUserJoined()),
                                new ApiMessageAttributes()),
                        response.getUsers(),
                        groups1);

                updates().executeAfter(response.getSeq(), () -> runOnUiThread(() -> callback.onResult(response.getGroup().getId())));
            }

            @Override
            public void onError(final RpcException e) {
                runOnUiThread(() -> callback.onError(e));
            }
        }, RPC_TIMEOUT);
    }

    public Command<List<PublicGroup>> listPublicGroups() {
        return callback -> request(new RequestGetPublicGroups(), new RpcCallback<ResponseGetPublicGroups>() {
            @Override
            public void onResult(ResponseGetPublicGroups response) {
                ArrayList<PublicGroup> groups1 = new ArrayList<>();
                for (ApiPublicGroup g : response.getGroups()) {
                    Avatar avatar = null;
                    if (g.getAvatar() != null) {
                        avatar = new Avatar(g.getAvatar());
                    }
                    groups1.add(new PublicGroup(g.getId(), g.getAccessHash(),
                            g.getTitle(), avatar, g.getDescription(), g.getMembersCount(), g.getFriendsCount()));
                }
                callback.onResult(groups1);
            }

            @Override
            public void onError(final RpcException e) {
                runOnUiThread(() -> callback.onError(e));
            }
        }, RPC_TIMEOUT);
    }

    public void resetModule() {
        groups.clear();
    }
}