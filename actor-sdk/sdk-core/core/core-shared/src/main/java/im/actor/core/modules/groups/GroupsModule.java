/*
 * Copyright (C) 2014-2015 Actor LLC. <https://actor.im>
 */

package im.actor.core.modules.groups;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import im.actor.core.ApiConfiguration;
import im.actor.core.api.ApiConfig;
import im.actor.core.api.ApiGroupOutPeer;
import im.actor.core.api.ApiMessageAttributes;
import im.actor.core.api.ApiOutPeer;
import im.actor.core.api.ApiPeer;
import im.actor.core.api.ApiPeerType;
import im.actor.core.api.ApiServiceExUserJoined;
import im.actor.core.api.ApiServiceMessage;
import im.actor.core.api.ApiUserOutPeer;
import im.actor.core.api.rpc.RequestCreateGroup;
import im.actor.core.api.rpc.RequestEditGroupAbout;
import im.actor.core.api.rpc.RequestEditGroupTitle;
import im.actor.core.api.rpc.RequestEditGroupTopic;
import im.actor.core.api.rpc.RequestGetGroupInviteUrl;
import im.actor.core.api.rpc.RequestGetIntegrationToken;
import im.actor.core.api.rpc.RequestInviteUser;
import im.actor.core.api.rpc.RequestJoinGroup;
import im.actor.core.api.rpc.RequestKickUser;
import im.actor.core.api.rpc.RequestLeaveGroup;
import im.actor.core.api.rpc.RequestMakeUserAdmin;
import im.actor.core.api.rpc.RequestMakeUserAdminObsolete;
import im.actor.core.api.rpc.RequestRevokeIntegrationToken;
import im.actor.core.api.rpc.RequestRevokeInviteUrl;
import im.actor.core.api.rpc.ResponseIntegrationToken;
import im.actor.core.api.rpc.ResponseInviteUrl;
import im.actor.core.api.updates.UpdateGroupAboutChanged;
import im.actor.core.api.updates.UpdateGroupAboutChangedObsolete;
import im.actor.core.api.updates.UpdateGroupMembersUpdate;
import im.actor.core.api.updates.UpdateGroupMembersUpdateObsolete;
import im.actor.core.api.updates.UpdateGroupTitleChanged;
import im.actor.core.api.updates.UpdateGroupTitleChangedObsolete;
import im.actor.core.api.updates.UpdateGroupTopicChanged;
import im.actor.core.api.updates.UpdateGroupTopicChangedObsolete;
import im.actor.core.api.updates.UpdateGroupUserInvited;
import im.actor.core.api.updates.UpdateGroupUserInvitedObsolete;
import im.actor.core.api.updates.UpdateGroupUserKick;
import im.actor.core.api.updates.UpdateGroupUserKickObsolete;
import im.actor.core.api.updates.UpdateGroupUserLeave;
import im.actor.core.api.updates.UpdateGroupUserLeaveObsolete;
import im.actor.core.api.updates.UpdateMessage;
import im.actor.core.entity.Group;
import im.actor.core.entity.User;
import im.actor.core.modules.AbsModule;
import im.actor.core.modules.ModuleContext;
import im.actor.core.modules.api.ApiSupportConfiguration;
import im.actor.core.modules.groups.router.GroupRouterInt;
import im.actor.core.modules.profile.avatar.GroupAvatarChangeActor;
import im.actor.core.util.RandomUtils;
import im.actor.core.viewmodel.GroupAvatarVM;
import im.actor.core.viewmodel.GroupVM;
import im.actor.runtime.Storage;
import im.actor.runtime.actors.ActorRef;
import im.actor.runtime.actors.messages.Void;
import im.actor.runtime.function.Function;
import im.actor.runtime.mvvm.MVVMCollection;
import im.actor.runtime.promise.Promise;
import im.actor.runtime.promise.Promises;
import im.actor.runtime.storage.KeyValueEngine;

import static im.actor.runtime.actors.ActorSystem.system;

public class GroupsModule extends AbsModule {

    private final KeyValueEngine<Group> groups;
    private final MVVMCollection<Group, GroupVM> collection;
    private final HashMap<Integer, GroupAvatarVM> avatarVMs;
    private final ActorRef avatarChangeActor;
    private final GroupRouterInt groupRouterInt;

    public GroupsModule(final ModuleContext context) {
        super(context);

        collection = Storage.createKeyValue(STORAGE_GROUPS, GroupVM.CREATOR(context.getAuthModule().myUid()), Group.CREATOR);
        groups = collection.getEngine();

        groupRouterInt = new GroupRouterInt(context);

        avatarVMs = new HashMap<>();
        avatarChangeActor = system().actorOf("actor/avatar/group", () -> new GroupAvatarChangeActor(context));
    }


    //
    // Storage
    //

    public KeyValueEngine<Group> getGroups() {
        return groups;
    }

    public GroupAvatarVM getAvatarVM(int gid) {
        synchronized (avatarVMs) {
            if (!avatarVMs.containsKey(gid)) {
                avatarVMs.put(gid, new GroupAvatarVM(gid));
            }
            return avatarVMs.get(gid);
        }
    }

    public MVVMCollection<Group, GroupVM> getGroupsCollection() {
        return collection;
    }

    public GroupRouterInt getRouter() {
        return groupRouterInt;
    }

    //
    // Actions
    //

    public Promise<Integer> createGroup(final String title, final String avatarDescriptor, final int[] uids) {
        return Promise.success(uids)
                .map((Function<int[], List<ApiUserOutPeer>>) ints -> {
                    ArrayList<ApiUserOutPeer> peers = new ArrayList<>();
                    for (int u : uids) {
                        User user = users().getValue(u);
                        if (user != null) {
                            peers.add(new ApiUserOutPeer(u, user.getAccessHash()));
                        }
                    }
                    return peers;
                })
                .flatMap(apiUserOutPeers ->
                        api(new RequestCreateGroup(RandomUtils.nextRid(), title, apiUserOutPeers, null, ApiSupportConfiguration.OPTIMIZATIONS)))
                .chain(response -> updates().applyRelatedData(response.getUsers(), response.getGroup()))
                .map(responseCreateGroup -> responseCreateGroup.getGroup().getId())
                .then(integer -> {
                    if (avatarDescriptor != null) {
                        changeAvatar(integer, avatarDescriptor);
                    }
                });
    }

    public Promise<Void> addMember(final int gid, final int uid) {
        final long rid = RandomUtils.nextRid();
        return Promises.tuple(getGroups().getValueAsync(gid), users().getValueAsync(uid))
                .flatMap(groupUserTuple2 ->
                        api(new RequestInviteUser(
                                new ApiGroupOutPeer(gid, groupUserTuple2.getT1().getAccessHash()),
                                rid,
                                new ApiUserOutPeer(uid, groupUserTuple2.getT2().getAccessHash()),
                                ApiSupportConfiguration.OPTIMIZATIONS)))
                .flatMap(responseSeqDate ->
                        updates().applyUpdate(
                                responseSeqDate.getSeq(),
                                responseSeqDate.getState(),
                                new UpdateGroupUserInvitedObsolete(
                                        gid, rid,
                                        uid, myUid(),
                                        responseSeqDate.getDate())
                        ));
    }

    public Promise<Void> kickMember(final int gid, final int uid) {
        final long rid = RandomUtils.nextRid();
        return Promises.tuple(getGroups().getValueAsync(gid), users().getValueAsync(uid))
                .flatMap(groupUserTuple2 ->
                        api(new RequestKickUser(
                                new ApiGroupOutPeer(gid, groupUserTuple2.getT1().getAccessHash()),
                                rid,
                                new ApiUserOutPeer(uid, groupUserTuple2.getT2().getAccessHash()),
                                ApiSupportConfiguration.OPTIMIZATIONS)))
                .flatMap(responseSeqDate ->
                        updates().applyUpdate(
                                responseSeqDate.getSeq(),
                                responseSeqDate.getState(),
                                new UpdateGroupUserKickObsolete(
                                        gid,
                                        rid,
                                        uid,
                                        myUid(),
                                        responseSeqDate.getDate())
                        ));
    }

    public Promise<Void> leaveGroup(final int gid) {
        final long rid = RandomUtils.nextRid();
        return getGroups().getValueAsync(gid)
                .flatMap(group ->
                        api(new RequestLeaveGroup(
                                new ApiGroupOutPeer(group.getGroupId(), group.getAccessHash()),
                                rid,
                                ApiSupportConfiguration.OPTIMIZATIONS)))
                .flatMap(responseSeqDate ->
                        updates().applyUpdate(
                                responseSeqDate.getSeq(),
                                responseSeqDate.getState(),
                                new UpdateGroupUserLeaveObsolete(
                                        gid,
                                        rid,
                                        myUid(),
                                        responseSeqDate.getDate())
                        ));
    }


    public Promise<Void> makeAdmin(final int gid, final int uid) {
        return Promises.tuple(getGroups().getValueAsync(gid), users().getValueAsync(uid))
                .flatMap(groupUserTuple2 -> api(new RequestMakeUserAdminObsolete(
                        new ApiGroupOutPeer(gid, groupUserTuple2.getT1().getAccessHash()),
                        new ApiUserOutPeer(uid, groupUserTuple2.getT2().getAccessHash()))))
                .flatMap(responseMakeUserAdmin ->
                        updates().applyUpdate(
                                responseMakeUserAdmin.getSeq(),
                                responseMakeUserAdmin.getState(),
                                new UpdateGroupMembersUpdateObsolete(gid, responseMakeUserAdmin.getMembers())
                        ));
    }


    public Promise<Void> editTitle(final int gid, final String name) {
        final long rid = RandomUtils.nextRid();
        return getGroups().getValueAsync(gid)
                .flatMap(group ->
                        api(new RequestEditGroupTitle(
                                new ApiGroupOutPeer(group.getGroupId(), group.getAccessHash()),
                                rid, name,
                                ApiSupportConfiguration.OPTIMIZATIONS)))
                .flatMap(responseSeqDate -> updates().applyUpdate(
                        responseSeqDate.getSeq(),
                        responseSeqDate.getState(),
                        new UpdateGroupTitleChangedObsolete(
                                gid, rid,
                                myUid(), name,
                                responseSeqDate.getDate()))
                );
    }

    public Promise<Void> editTheme(final int gid, final String theme) {
        final long rid = RandomUtils.nextRid();
        return getGroups().getValueAsync(gid)
                .flatMap(group ->
                        api(new RequestEditGroupTopic(
                                new ApiGroupOutPeer(group.getGroupId(), group.getAccessHash()),
                                rid, theme,
                                ApiSupportConfiguration.OPTIMIZATIONS)))
                .flatMap(responseSeqDate -> updates().applyUpdate(
                        responseSeqDate.getSeq(),
                        responseSeqDate.getState(),
                        new UpdateGroupTopicChangedObsolete(
                                gid, rid,
                                myUid(), theme,
                                responseSeqDate.getDate())
                ));
    }

    public Promise<Void> editAbout(final int gid, final String about) {
        final long rid = RandomUtils.nextRid();
        return getGroups().getValueAsync(gid)
                .flatMap(group ->
                        api(new RequestEditGroupAbout(
                                new ApiGroupOutPeer(group.getGroupId(), group.getAccessHash()),
                                rid, about, ApiSupportConfiguration.OPTIMIZATIONS)))
                .flatMap(responseSeqDate -> updates().applyUpdate(
                        responseSeqDate.getSeq(),
                        responseSeqDate.getState(),
                        new UpdateGroupAboutChangedObsolete(
                                gid, about)
                ));
    }

    public void changeAvatar(int gid, String descriptor) {
        avatarChangeActor.send(new GroupAvatarChangeActor.ChangeAvatar(gid, descriptor));
    }

    public void removeAvatar(int gid) {
        avatarChangeActor.send(new GroupAvatarChangeActor.RemoveAvatar(gid));
    }


    //
    // Join
    //

    public Promise<String> requestInviteLink(final int gid) {
        return getGroups().getValueAsync(gid)
                .flatMap(group ->
                        api(new RequestGetGroupInviteUrl(
                                new ApiGroupOutPeer(group.getGroupId(), group.getAccessHash()))))
                .map(ResponseInviteUrl::getUrl);
    }

    public Promise<String> requestRevokeLink(final int gid) {
        return getGroups().getValueAsync(gid)
                .flatMap(group ->
                        api(new RequestRevokeInviteUrl(
                                new ApiGroupOutPeer(group.getGroupId(), group.getAccessHash()))))
                .map(ResponseInviteUrl::getUrl);
    }

    public Promise<Integer> joinGroupByToken(final String token) {
        return api(new RequestJoinGroup(token, ApiSupportConfiguration.OPTIMIZATIONS))
                .chain(responseJoinGroup ->
                        updates().applyUpdate(
                                responseJoinGroup.getSeq(),
                                responseJoinGroup.getState(),
                                new UpdateMessage(
                                        new ApiPeer(ApiPeerType.GROUP, responseJoinGroup.getGroup().getId()),
                                        myUid(),
                                        responseJoinGroup.getDate(),
                                        responseJoinGroup.getRid(),
                                        new ApiServiceMessage("Joined chat",
                                                new ApiServiceExUserJoined()),
                                        new ApiMessageAttributes(),
                                        null),
                                responseJoinGroup.getUsers(),
                                responseJoinGroup.getGroup()
                        )
                )
                .map(responseJoinGroup -> responseJoinGroup.getGroup().getId());
    }


    //
    // Integration Token
    //

    public Promise<String> requestIntegrationToken(final int gid) {
        return getGroups().getValueAsync(gid)
                .flatMap(group ->
                        api(new RequestGetIntegrationToken(
                                new ApiOutPeer(
                                        ApiPeerType.GROUP,
                                        group.getGroupId(),
                                        group.getAccessHash()))))
                .map(ResponseIntegrationToken::getUrl);
    }

    public Promise<String> revokeIntegrationToken(final int gid) {
        return getGroups().getValueAsync(gid)
                .flatMap(group ->
                        api(new RequestRevokeIntegrationToken(
                                new ApiOutPeer(
                                        ApiPeerType.GROUP,
                                        group.getGroupId(),
                                        group.getAccessHash()))))
                .map(ResponseIntegrationToken::getUrl);
    }


    public void resetModule() {
        groups.clear();
    }
}