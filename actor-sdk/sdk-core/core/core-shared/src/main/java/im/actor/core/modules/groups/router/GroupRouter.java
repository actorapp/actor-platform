package im.actor.core.modules.groups.router;

import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import im.actor.core.api.ApiAvatar;
import im.actor.core.api.ApiGroup;
import im.actor.core.api.ApiGroupOutPeer;
import im.actor.core.api.ApiMapValue;
import im.actor.core.api.ApiMember;
import im.actor.core.api.rpc.RequestLoadFullGroups;
import im.actor.core.api.updates.UpdateGroupAboutChanged;
import im.actor.core.api.updates.UpdateGroupAvatarChanged;
import im.actor.core.api.updates.UpdateGroupDeleted;
import im.actor.core.api.updates.UpdateGroupExtChanged;
import im.actor.core.api.updates.UpdateGroupFullExtChanged;
import im.actor.core.api.updates.UpdateGroupFullPermissionsChanged;
import im.actor.core.api.updates.UpdateGroupHistoryShared;
import im.actor.core.api.updates.UpdateGroupMemberAdminChanged;
import im.actor.core.api.updates.UpdateGroupMemberChanged;
import im.actor.core.api.updates.UpdateGroupMemberDiff;
import im.actor.core.api.updates.UpdateGroupMembersBecameAsync;
import im.actor.core.api.updates.UpdateGroupMembersCountChanged;
import im.actor.core.api.updates.UpdateGroupMembersUpdated;
import im.actor.core.api.updates.UpdateGroupOwnerChanged;
import im.actor.core.api.updates.UpdateGroupPermissionsChanged;
import im.actor.core.api.updates.UpdateGroupShortNameChanged;
import im.actor.core.api.updates.UpdateGroupTitleChanged;
import im.actor.core.api.updates.UpdateGroupTopicChanged;
import im.actor.core.entity.Group;
import im.actor.core.modules.ModuleActor;
import im.actor.core.modules.ModuleContext;
import im.actor.core.modules.groups.router.entity.RouterApplyGroups;
import im.actor.core.modules.groups.router.entity.RouterFetchMissingGroups;
import im.actor.core.modules.groups.router.entity.RouterGroupUpdate;
import im.actor.core.modules.groups.router.entity.RouterLoadFullGroup;
import im.actor.core.modules.messaging.router.RouterInt;
import im.actor.core.network.parser.Update;
import im.actor.runtime.actors.messages.Void;
import im.actor.runtime.annotations.Verified;
import im.actor.runtime.function.Function;
import im.actor.runtime.function.Tuple2;
import im.actor.runtime.promise.Promise;
import im.actor.runtime.promise.PromisesArray;

public class GroupRouter extends ModuleActor {

    // j2objc workaround
    private static final Void DUMB = null;

    private final HashSet<Integer> requestedFullGroups = new HashSet<>();
    private boolean isFreezed = false;

    public GroupRouter(ModuleContext context) {
        super(context);
    }

    //
    // Updates Main
    //

    @Verified
    public Promise<Void> onAvatarChanged(int groupId, @Nullable ApiAvatar avatar) {
        return editDescGroup(groupId, group -> group.editAvatar(avatar));
    }

    @Verified
    public Promise<Void> onTitleChanged(int groupId, String title) {
        return editDescGroup(groupId, group -> group.editTitle(title));
    }

    @Verified
    public Promise<Void> onIsMemberChanged(int groupId, boolean isMember) {
        return editGroup(groupId, group -> group.editIsMember(isMember));
    }

    @Verified
    public Promise<Void> onPermissionsChanged(int groupId, long permissions) {
        return editGroup(groupId, group -> group.editPermissions(permissions));
    }

    @Verified
    public Promise<Void> onGroupDeleted(int groupId) {
        return editGroup(groupId, group -> group.editIsDeleted(true));
    }

    @Verified
    public Promise<Void> onExtChanged(int groupId, ApiMapValue ext) {
        return editGroup(groupId, group -> group.editExt(ext));
    }

    //
    // Members Updates
    //

    @Verified
    public Promise<Void> onMembersChanged(int groupId, List<ApiMember> members) {
        return editGroup(groupId, group -> group.editMembers(members));
    }

    @Verified
    public Promise<Void> onMembersChanged(int groupId, List<ApiMember> added, List<Integer> removed, int count) {
        return editGroup(groupId, group -> group.editMembers(added, removed, count));
    }

    @Verified
    public Promise<Void> onMembersChanged(int groupId, int membersCount) {
        return editGroup(groupId, group -> group.editMembersCount(membersCount));
    }

    @Verified
    public Promise<Void> onMembersBecameAsync(int groupId) {
        return editGroup(groupId, group -> group.editMembersBecameAsync());
    }

    @Verified
    public Promise<Void> onMemberChangedAdmin(int groupId, int uid, Boolean isAdmin) {
        return editGroup(groupId, group -> group.editMemberChangedAdmin(uid, isAdmin));
    }

    //
    // Updates Ext
    //

    @Verified
    public Promise<Void> onTopicChanged(int groupId, String topic) {
        return editGroup(groupId, group -> group.editTopic(topic));
    }

    @Verified
    public Promise<Void> onAboutChanged(int groupId, String about) {
        return editGroup(groupId, group -> group.editAbout(about));
    }

    @Verified
    public Promise<Void> onShortNameChanged(int groupId, String shortName) {
        return editGroup(groupId, group -> group.editShortName(shortName));
    }


    @Verified
    public Promise<Void> onOwnerChanged(int groupId, int updatedOwner) {
        return editGroup(groupId, group -> group.editOwner(updatedOwner));
    }

    @Verified
    public Promise<Void> onHistoryShared(int groupId) {
        return editGroup(groupId, group -> group.editHistoryShared());
    }

    @Verified
    public Promise<Void> onFullPermissionsChanged(int groupId, long permissions) {
        return editGroup(groupId, group -> group.editExtPermissions(permissions));
    }

    @Verified
    public Promise<Void> onFullExtChanged(int groupId, ApiMapValue ext) {
        return editGroup(groupId, group -> group.editFullExt(ext));
    }

    //
    // Wrapper
    //

    private Promise<Void> forGroup(int groupId, Function<Group, Promise<Void>> func) {
        freeze();
        return groups().getValueAsync(groupId)
                .fallback(e -> null)
                .flatMap(g -> {
                    if (g != null) {
                        return func.apply(g);
                    }
                    return Promise.success(null);
                })
                .after((v, e) -> {
                    unfreeze();
                });
    }

    private Promise<Void> editGroup(int groupId, Function<Group, Group> func) {
        return forGroup(groupId, group -> {
            groups().addOrUpdateItem(func.apply(group));
            return Promise.success(null);
        });
    }

    private Promise<Void> editDescGroup(int groupId, Function<Group, Group> func) {
        return forGroup(groupId, group -> {
            Group g = func.apply(group);
            groups().addOrUpdateItem(g);
            return onGroupDescChanged(g);
        });
    }

    //
    // Entities
    //

    @Verified
    private Promise<List<ApiGroupOutPeer>> fetchMissingGroups(List<ApiGroupOutPeer> groups) {
        freeze();
        return PromisesArray.of(groups)
                .map((Function<ApiGroupOutPeer, Promise<ApiGroupOutPeer>>) u -> groups().containsAsync(u.getGroupId())
                        .map(v -> v ? null : u))
                .filterNull()
                .zip()
                .after((r, e) -> {
                    unfreeze();
                });
    }

    @Verified
    private Promise<Void> applyGroups(List<ApiGroup> groups) {
        freeze();
        return PromisesArray.of(groups)
                .map((Function<ApiGroup, Promise<Tuple2<ApiGroup, Boolean>>>) u -> groups().containsAsync(u.getId())
                        .map(v -> new Tuple2<>(u, v)))
                .filter(t -> !t.getT2())
                .zip()
                .then(x -> {
                    List<Group> res = new ArrayList<>();
                    for (Tuple2<ApiGroup, Boolean> u : x) {
                        res.add(new Group(u.getT1(), null));
                    }
                    if (res.size() > 0) {
                        groups().addOrUpdateItems(res);
                    }
                })
                .map(x -> (Void) null)
                .after((r, e) -> unfreeze());
    }

    private void onRequestLoadFullGroup(int gid) {
        if (requestedFullGroups.contains(gid)) {
            return;
        }
        requestedFullGroups.add(gid);

        freeze();
        groups().getValueAsync(gid)
                // Do not reduce to lambda due j2objc bug
                .flatMap(new Function<Group, Promise<Group>>() {
                    @Override
                    public Promise<Group> apply(Group group) {
                        if (!group.isHaveExtension()) {
                            ArrayList<ApiGroupOutPeer> groups = new ArrayList<>();
                            groups.add(new ApiGroupOutPeer(gid, group.getAccessHash()));
                            return api(new RequestLoadFullGroups(groups))
                                    .map(r -> group.updateExt(r.getGroups().get(0)));
                        } else {
                            return Promise.failure(new RuntimeException("Already loaded"));
                        }
                    }
                })
                .then(r -> groups().addOrUpdateItem(r))
                .after((r, e) -> unfreeze());
    }

    //
    // Tools
    //

    @Verified
    private Promise<Void> onGroupDescChanged(Group group) {
        return getRouter().onGroupChanged(group);
    }

    private RouterInt getRouter() {
        return context().getMessagesModule().getRouter();
    }

    private void freeze() {
        isFreezed = true;
    }

    private void unfreeze() {
        isFreezed = false;
        unstashAll();
    }


    //
    // Messages
    //

    private Promise<Void> onUpdate(Update update) {

        //
        // Main
        //

        if (update instanceof UpdateGroupTitleChanged) {
            UpdateGroupTitleChanged titleChanged = (UpdateGroupTitleChanged) update;
            return onTitleChanged(titleChanged.getGroupId(), titleChanged.getTitle());
        } else if (update instanceof UpdateGroupAvatarChanged) {
            UpdateGroupAvatarChanged avatarChanged = (UpdateGroupAvatarChanged) update;
            return onAvatarChanged(avatarChanged.getGroupId(), avatarChanged.getAvatar());
        } else if (update instanceof UpdateGroupMemberChanged) {
            UpdateGroupMemberChanged memberChanged = (UpdateGroupMemberChanged) update;
            return onIsMemberChanged(memberChanged.getGroupId(), memberChanged.isMember());
        } else if (update instanceof UpdateGroupPermissionsChanged) {
            UpdateGroupPermissionsChanged permissionsChanged = (UpdateGroupPermissionsChanged) update;
            return onPermissionsChanged(permissionsChanged.getGroupId(), permissionsChanged.getPermissions());
        } else if (update instanceof UpdateGroupDeleted) {
            UpdateGroupDeleted groupDeleted = (UpdateGroupDeleted) update;
            return onGroupDeleted(groupDeleted.getGroupId());
        } else if (update instanceof UpdateGroupExtChanged) {
            UpdateGroupExtChanged extChanged = (UpdateGroupExtChanged) update;
            return onExtChanged(extChanged.getGroupId(), extChanged.getExt());
        }

        //
        // Members
        //

        else if (update instanceof UpdateGroupMembersUpdated) {
            UpdateGroupMembersUpdated membersUpdate = (UpdateGroupMembersUpdated) update;
            return onMembersChanged(membersUpdate.getGroupId(), membersUpdate.getMembers());
        } else if (update instanceof UpdateGroupMemberAdminChanged) {
            UpdateGroupMemberAdminChanged adminChanged = (UpdateGroupMemberAdminChanged) update;
            return onMemberChangedAdmin(adminChanged.getGroupId(), adminChanged.getUserId(),
                    adminChanged.isAdmin());
        } else if (update instanceof UpdateGroupMemberDiff) {
            UpdateGroupMemberDiff memberDiff = (UpdateGroupMemberDiff) update;
            return onMembersChanged(memberDiff.getGroupId(), memberDiff.getAddedMembers(),
                    memberDiff.getRemovedUsers(), memberDiff.getMembersCount());
        } else if (update instanceof UpdateGroupMembersBecameAsync) {
            UpdateGroupMembersBecameAsync becameAsync = (UpdateGroupMembersBecameAsync) update;
            return onMembersBecameAsync(becameAsync.getGroupId());
        } else if (update instanceof UpdateGroupMembersCountChanged) {
            UpdateGroupMembersCountChanged membersCountChanged = (UpdateGroupMembersCountChanged) update;
            return onMembersChanged(membersCountChanged.getGroupId(), membersCountChanged.getMembersCount());
        }

        //
        // Ext
        //

        else if (update instanceof UpdateGroupTopicChanged) {
            UpdateGroupTopicChanged topicChanged = (UpdateGroupTopicChanged) update;
            return onTopicChanged(topicChanged.getGroupId(), topicChanged.getTopic());
        } else if (update instanceof UpdateGroupAboutChanged) {
            UpdateGroupAboutChanged aboutChanged = (UpdateGroupAboutChanged) update;
            return onAboutChanged(aboutChanged.getGroupId(), aboutChanged.getAbout());
        } else if (update instanceof UpdateGroupHistoryShared) {
            UpdateGroupHistoryShared historyShared = (UpdateGroupHistoryShared) update;
            return onHistoryShared(historyShared.getGroupId());
        } else if (update instanceof UpdateGroupOwnerChanged) {
            UpdateGroupOwnerChanged ownerChanged = (UpdateGroupOwnerChanged) update;
            return onOwnerChanged(ownerChanged.getGroupId(), ownerChanged.getUserId());
        } else if (update instanceof UpdateGroupShortNameChanged) {
            UpdateGroupShortNameChanged shortNameChanged = (UpdateGroupShortNameChanged) update;
            return onShortNameChanged(shortNameChanged.getGroupId(), shortNameChanged.getShortName());
        } else if (update instanceof UpdateGroupFullPermissionsChanged) {
            UpdateGroupFullPermissionsChanged permissionsChanged = (UpdateGroupFullPermissionsChanged) update;
            return onFullPermissionsChanged(permissionsChanged.getGroupId(), permissionsChanged.getPermissions());
        } else if (update instanceof UpdateGroupFullExtChanged) {
            UpdateGroupFullExtChanged extChanged = (UpdateGroupFullExtChanged) update;
            return onFullExtChanged(extChanged.getGroupId(), extChanged.getExt());
        }

        return Promise.success(null);
    }

    @Override
    public Promise onAsk(Object message) throws Exception {
        if (message instanceof RouterGroupUpdate) {
            if (isFreezed) {
                stash();
                return null;
            }
            return onUpdate(((RouterGroupUpdate) message).getUpdate());
        } else if (message instanceof RouterApplyGroups) {
            if (isFreezed) {
                stash();
                return null;
            }
            return applyGroups(((RouterApplyGroups) message).getGroups());
        } else if (message instanceof RouterFetchMissingGroups) {
            if (isFreezed) {
                stash();
                return null;
            }
            return fetchMissingGroups(((RouterFetchMissingGroups) message).getGroups());
        } else {
            return super.onAsk(message);
        }
    }

    @Override
    public void onReceive(Object message) {

        if (message instanceof RouterLoadFullGroup) {
            if (isFreezed) {
                stash();
                return;
            }
            onRequestLoadFullGroup(((RouterLoadFullGroup) message).getGid());
        } else {
            super.onReceive(message);
        }
    }
}
