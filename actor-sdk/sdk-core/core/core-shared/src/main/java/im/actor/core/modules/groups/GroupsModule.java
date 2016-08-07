/*
 * Copyright (C) 2014-2015 Actor LLC. <https://actor.im>
 */

package im.actor.core.modules.groups;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import im.actor.core.api.ApiAdminSettings;
import im.actor.core.api.ApiGroupOutPeer;
import im.actor.core.api.ApiGroupType;
import im.actor.core.api.ApiMember;
import im.actor.core.api.ApiOutPeer;
import im.actor.core.api.ApiPeerType;
import im.actor.core.api.ApiUserOutPeer;
import im.actor.core.api.rpc.RequestCreateGroup;
import im.actor.core.api.rpc.RequestDeleteGroup;
import im.actor.core.api.rpc.RequestDismissUserAdmin;
import im.actor.core.api.rpc.RequestEditGroupAbout;
import im.actor.core.api.rpc.RequestEditGroupShortName;
import im.actor.core.api.rpc.RequestEditGroupTitle;
import im.actor.core.api.rpc.RequestEditGroupTopic;
import im.actor.core.api.rpc.RequestGetGroupInviteUrl;
import im.actor.core.api.rpc.RequestGetIntegrationToken;
import im.actor.core.api.rpc.RequestInviteUser;
import im.actor.core.api.rpc.RequestJoinGroup;
import im.actor.core.api.rpc.RequestJoinGroupByPeer;
import im.actor.core.api.rpc.RequestKickUser;
import im.actor.core.api.rpc.RequestLeaveAndDelete;
import im.actor.core.api.rpc.RequestLeaveGroup;
import im.actor.core.api.rpc.RequestLoadAdminSettings;
import im.actor.core.api.rpc.RequestLoadMembers;
import im.actor.core.api.rpc.RequestMakeUserAdminObsolete;
import im.actor.core.api.rpc.RequestRevokeIntegrationToken;
import im.actor.core.api.rpc.RequestRevokeInviteUrl;
import im.actor.core.api.rpc.RequestSaveAdminSettings;
import im.actor.core.api.rpc.RequestShareHistory;
import im.actor.core.api.rpc.RequestTransferOwnership;
import im.actor.core.api.rpc.ResponseIntegrationToken;
import im.actor.core.api.rpc.ResponseInviteUrl;
import im.actor.core.api.rpc.ResponseVoid;
import im.actor.core.entity.Group;
import im.actor.core.entity.GroupMember;
import im.actor.core.entity.GroupMembersSlice;
import im.actor.core.entity.GroupPermissions;
import im.actor.core.entity.Peer;
import im.actor.core.entity.PeerType;
import im.actor.core.entity.User;
import im.actor.core.events.PeerChatOpened;
import im.actor.core.events.PeerInfoOpened;
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
import im.actor.runtime.eventbus.BusSubscriber;
import im.actor.runtime.eventbus.Event;
import im.actor.runtime.function.Function;
import im.actor.runtime.mvvm.MVVMCollection;
import im.actor.runtime.promise.Promise;
import im.actor.runtime.promise.Promises;
import im.actor.runtime.storage.KeyValueEngine;

import static im.actor.runtime.actors.ActorSystem.system;

public class GroupsModule extends AbsModule implements BusSubscriber {

    // Workaround for j2objc bug
    private static final Void DUMB = null;
    private static final ResponseVoid DUMB2 = null;

    private final KeyValueEngine<Group> groups;
    private final MVVMCollection<Group, GroupVM> collection;
    private final HashMap<Integer, GroupAvatarVM> avatarVMs;
    private final ActorRef avatarChangeActor;
    private final GroupRouterInt groupRouterInt;

    public GroupsModule(final ModuleContext context) {
        super(context);

        collection = Storage.createKeyValue(STORAGE_GROUPS, GroupVM.CREATOR, Group.CREATOR);
        groups = collection.getEngine();

        groupRouterInt = new GroupRouterInt(context);

        avatarVMs = new HashMap<>();
        avatarChangeActor = system().actorOf("actor/avatar/group", () -> new GroupAvatarChangeActor(context));
    }

    public void run() {
        context().getEvents().subscribe(this, PeerChatOpened.EVENT);
        context().getEvents().subscribe(this, PeerInfoOpened.EVENT);
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

    public Promise<Integer> createGroup(String title, String avatarDescriptor, int[] uids) {
        return createGroup(title, avatarDescriptor, uids, ApiGroupType.GROUP);
    }

    public Promise<Integer> createChannel(String title, String avatarDescriptor) {
        return createGroup(title, avatarDescriptor, new int[0], ApiGroupType.CHANNEL);
    }

    private Promise<Integer> createGroup(String title, String avatarDescriptor, int[] uids,
                                         ApiGroupType groupType) {
        long rid = RandomUtils.nextRid();
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
                        api(new RequestCreateGroup(rid, title, apiUserOutPeers,
                                groupType, ApiSupportConfiguration.OPTIMIZATIONS)))
                .chain(r -> updates().applyRelatedData(r.getUsers(), r.getGroup()))
                .chain(r -> updates().waitForUpdate(r.getSeq()))
                .map(r -> r.getGroup().getId())
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
                .flatMap(r -> updates().waitForUpdate(r.getSeq()));
    }

    public Promise<Void> kickMember(int gid, int uid) {
        final long rid = RandomUtils.nextRid();
        return Promises.tuple(getGroups().getValueAsync(gid), users().getValueAsync(uid))
                .flatMap(groupUserTuple2 ->
                        api(new RequestKickUser(
                                new ApiGroupOutPeer(gid, groupUserTuple2.getT1().getAccessHash()),
                                rid,
                                new ApiUserOutPeer(uid, groupUserTuple2.getT2().getAccessHash()),
                                ApiSupportConfiguration.OPTIMIZATIONS)))
                .flatMap(r -> updates().waitForUpdate(r.getSeq()));
    }

    public Promise<Void> leaveGroup(int gid) {
        final long rid = RandomUtils.nextRid();
        return getGroups().getValueAsync(gid)
                .flatMap(group ->
                        api(new RequestLeaveGroup(
                                new ApiGroupOutPeer(group.getGroupId(), group.getAccessHash()),
                                rid,
                                ApiSupportConfiguration.OPTIMIZATIONS)))
                .flatMap(r -> updates().waitForUpdate(r.getSeq()));
    }

    public Promise<Void> leaveAndDeleteGroup(int gid) {
        return getGroups().getValueAsync(gid)
                .flatMap(group ->
                        api(new RequestLeaveAndDelete(
                                new ApiGroupOutPeer(group.getGroupId(), group.getAccessHash()))))
                .flatMap(r -> updates().waitForUpdate(r.getSeq()));
    }

    public Promise<Void> deleteGroup(int gid) {
        return getGroups().getValueAsync(gid)
                .flatMap(group ->
                        api(new RequestDeleteGroup(new ApiGroupOutPeer(group.getGroupId(), group.getAccessHash()))))
                .flatMap(r -> updates().waitForUpdate(r.getSeq()));
    }

    public Promise<Void> shareHistory(int gid) {
        return getGroups().getValueAsync(gid)
                .flatMap(group ->
                        api(new RequestShareHistory(new ApiGroupOutPeer(group.getGroupId(), group.getAccessHash()))))
                .flatMap(r -> updates().waitForUpdate(r.getSeq()));
    }

    public Promise<Void> makeAdmin(final int gid, final int uid) {
        return Promises.tuple(getGroups().getValueAsync(gid), users().getValueAsync(uid))
                .flatMap(groupUserTuple2 -> api(new RequestMakeUserAdminObsolete(
                        new ApiGroupOutPeer(gid, groupUserTuple2.getT1().getAccessHash()),
                        new ApiUserOutPeer(uid, groupUserTuple2.getT2().getAccessHash()))))
                .flatMap(r -> updates().waitForUpdate(r.getSeq()));
    }

    public Promise<Void> revokeAdmin(final int gid, final int uid) {
        return Promises.tuple(getGroups().getValueAsync(gid), users().getValueAsync(uid))
                .flatMap(groupUserTuple2 -> api(new RequestDismissUserAdmin(
                        new ApiGroupOutPeer(gid, groupUserTuple2.getT1().getAccessHash()),
                        new ApiUserOutPeer(uid, groupUserTuple2.getT2().getAccessHash()))))
                .flatMap(r -> updates().waitForUpdate(r.getSeq()));
    }

    public Promise<Void> transferOwnership(final int gid, final int uid) {
        return Promises.tuple(getGroups().getValueAsync(gid), users().getValueAsync(uid))
                .flatMap(groupUserTuple2 -> api(new RequestTransferOwnership(
                        new ApiGroupOutPeer(gid, groupUserTuple2.getT1().getAccessHash()),
                        new ApiUserOutPeer(uid, groupUserTuple2.getT2().getAccessHash()))))
                .flatMap(r -> updates().waitForUpdate(r.getSeq()));
    }

    public Promise<Void> editTitle(final int gid, final String name) {
        final long rid = RandomUtils.nextRid();
        return getGroups().getValueAsync(gid)
                .flatMap(group ->
                        api(new RequestEditGroupTitle(
                                new ApiGroupOutPeer(group.getGroupId(), group.getAccessHash()),
                                rid, name,
                                ApiSupportConfiguration.OPTIMIZATIONS)))
                .flatMap(r -> updates().waitForUpdate(r.getSeq()));
    }

    public Promise<Void> editTheme(final int gid, final String theme) {
        final long rid = RandomUtils.nextRid();
        return getGroups().getValueAsync(gid)
                .flatMap(group ->
                        api(new RequestEditGroupTopic(
                                new ApiGroupOutPeer(group.getGroupId(), group.getAccessHash()),
                                rid, theme,
                                ApiSupportConfiguration.OPTIMIZATIONS)))
                .flatMap(r -> updates().waitForUpdate(r.getSeq()));
    }

    public Promise<Void> editAbout(final int gid, final String about) {
        final long rid = RandomUtils.nextRid();
        return getGroups().getValueAsync(gid)
                .flatMap(group ->
                        api(new RequestEditGroupAbout(
                                new ApiGroupOutPeer(group.getGroupId(), group.getAccessHash()),
                                rid, about, ApiSupportConfiguration.OPTIMIZATIONS)))
                .flatMap(r -> updates().waitForUpdate(r.getSeq()));
    }

    public Promise<Void> editShortName(final int gid, final String shortName) {
        return getGroups().getValueAsync(gid)
                .flatMap(group ->
                        api(new RequestEditGroupShortName(new ApiGroupOutPeer(group.getGroupId(), group.getAccessHash()),
                                shortName)))
                .flatMap(r -> updates().waitForUpdate(r.getSeq()));
    }

    public Promise<GroupPermissions> loadAdminSettings(int gid) {
        return getGroups().getValueAsync(gid)
                .flatMap(group -> api(new RequestLoadAdminSettings(new ApiGroupOutPeer(group.getGroupId(), group.getAccessHash()))))
                .map(r -> new GroupPermissions(r.getSettings()));
    }

    public Promise<Void> saveAdminSettings(int gid, GroupPermissions adminSettings) {
        return getGroups().getValueAsync(gid)
                .flatMap(group -> api(new RequestSaveAdminSettings(
                        new ApiGroupOutPeer(group.getGroupId(), group.getAccessHash()),
                        adminSettings.getApiSettings())))
                .map(r -> null);
    }

    public Promise<GroupMembersSlice> loadMembers(int gid, int limit, byte[] next) {
        return getGroups().getValueAsync(gid)
                .flatMap(group ->
                        api(new RequestLoadMembers(
                                new ApiGroupOutPeer(group.getGroupId(), group.getAccessHash()),
                                limit, next)))
                .chain(r -> updates().loadRequiredPeers(r.getUsers(), new ArrayList<>()))
                .map(r -> {
                    ArrayList<GroupMember> members = new ArrayList<>();
                    for (ApiMember p : r.getMembers()) {
                        boolean isAdmin = p.isAdmin() != null ? p.isAdmin() : false;
                        members.add(new GroupMember(p.getUid(),
                                p.getInviterUid(), p.getInviterUid(), isAdmin));
                    }
                    return new GroupMembersSlice(members, r.getNext());
                });
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
                .chain(responseJoinGroup -> updates().loadRequiredPeers(responseJoinGroup.getUserPeers(), new ArrayList<>()))
                .chain(r -> updates().waitForUpdate(r.getSeq()))
                .map(responseJoinGroup -> responseJoinGroup.getGroup().getId());
    }

    public Promise<Void> joinGroup(int gid) {
        return getGroups().getValueAsync(gid)
                .flatMap(group ->
                        api(new RequestJoinGroupByPeer(
                                new ApiGroupOutPeer(group.getGroupId(), group.getAccessHash()))))
                .chain(r -> updates().waitForUpdate(r.getSeq()))
                .map(r -> null);
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

    @Override
    public void onBusEvent(Event event) {
        if (event instanceof PeerChatOpened) {
            Peer peer = ((PeerChatOpened) event).getPeer();
            if (peer.getPeerType() == PeerType.GROUP) {
                getRouter().onFullGroupNeeded(peer.getPeerId());
            }
        } else if (event instanceof PeerInfoOpened) {
            Peer peer = ((PeerInfoOpened) event).getPeer();
            if (peer.getPeerType() == PeerType.GROUP) {
                getRouter().onFullGroupNeeded(peer.getPeerId());
            }
        }
    }
}