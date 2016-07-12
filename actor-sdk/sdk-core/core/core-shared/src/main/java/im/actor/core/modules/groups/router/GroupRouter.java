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
import im.actor.core.api.updates.UpdateGroupAboutChangedObsolete;
import im.actor.core.api.updates.UpdateGroupAvatarChanged;
import im.actor.core.api.updates.UpdateGroupCanSendMessagesChanged;
import im.actor.core.api.updates.UpdateGroupExtChanged;
import im.actor.core.api.updates.UpdateGroupInviteObsolete;
import im.actor.core.api.updates.UpdateGroupMemberAdminChanged;
import im.actor.core.api.updates.UpdateGroupMemberChanged;
import im.actor.core.api.updates.UpdateGroupMemberDiff;
import im.actor.core.api.updates.UpdateGroupMembersBecameAsync;
import im.actor.core.api.updates.UpdateGroupMembersCountChanged;
import im.actor.core.api.updates.UpdateGroupMembersUpdate;
import im.actor.core.api.updates.UpdateGroupMembersUpdateObsolete;
import im.actor.core.api.updates.UpdateGroupTitleChanged;
import im.actor.core.api.updates.UpdateGroupTopicChangedObsolete;
import im.actor.core.api.updates.UpdateGroupUserInvitedObsolete;
import im.actor.core.api.updates.UpdateGroupUserKickObsolete;
import im.actor.core.api.updates.UpdateGroupUserLeaveObsolete;
import im.actor.core.entity.Group;
import im.actor.core.entity.Message;
import im.actor.core.entity.MessageState;
import im.actor.core.entity.content.ServiceGroupCreated;
import im.actor.core.entity.content.ServiceGroupUserInvited;
import im.actor.core.entity.content.ServiceGroupUserKicked;
import im.actor.core.entity.content.ServiceGroupUserLeave;
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
        return forGroup(groupId, group -> {
            Group upd = group.editAvatar(avatar);
            groups().addOrUpdateItem(upd);
            return onGroupDescChanged(upd);
        });
    }

    @Verified
    public Promise<Void> onTitleChanged(int groupId, String title) {
        return forGroup(groupId, group -> {
            Group upd = group.editTitle(title);
            groups().addOrUpdateItem(upd);
            return onGroupDescChanged(upd);
        });
    }

    @Verified
    public Promise<Void> onCanWriteMessagesChanged(int groupId, boolean canWrite) {
        return forGroup(groupId, group -> {
            Group upd = group.editCanWrite(canWrite);
            groups().addOrUpdateItem(upd);
            return Promise.success(null);
        });
    }

    @Verified
    public Promise<Void> onIsMemberChanged(int groupId, boolean isMember) {
        return forGroup(groupId, group -> {
            Group upd = group.editIsMember(isMember);
            groups().addOrUpdateItem(upd);
            return Promise.success(null);
        });
    }

    @Verified
    public Promise<Void> onExtChanged(int groupId, ApiMapValue ext) {
        return forGroup(groupId, group -> {
            Group upd = group.editExt(ext);
            groups().addOrUpdateItem(upd);
            return Promise.success(null);
        });
    }

    //
    // Members Updates
    //


    //
    // Updates Ext
    //

    @Verified
    public Promise<Void> onGroupInvite(int groupId, long rid, int inviterId, long date, boolean isSilent) {
        return forGroup(groupId, group -> {

            groups().addOrUpdateItem(group
                    .addMember(myUid(), inviterId, date));

            if (!isSilent) {
                if (inviterId == myUid()) {
                    // If current user invite himself, add create group message
                    Message message = new Message(rid, date, date, inviterId,
                            MessageState.UNKNOWN, ServiceGroupCreated.create());
                    return getRouter().onNewMessage(group.peer(), message);
                } else {
                    // else add invite message
                    Message message = new Message(rid, date, date, inviterId,
                            MessageState.SENT, ServiceGroupUserInvited.create(myUid()));
                    return getRouter().onNewMessage(group.peer(), message);
                }
            }

            return Promise.success(null);
        });
    }

    @Verified
    public Promise<Void> onUserLeave(int groupId, long rid, int uid, long date, boolean isSilent) {
        return forGroup(groupId, group -> {

            if (uid == myUid()) {
                // If current user leave, clear members and change member state
                groups().addOrUpdateItem(group
                        .clearMembers());
            } else {
                // else remove leaved user
                groups().addOrUpdateItem(group
                        .removeMember(uid));
            }

            // Create message if needed
            if (!isSilent) {
                Message message = new Message(rid, date, date, uid,
                        uid == myUid() ? MessageState.SENT : MessageState.UNKNOWN,
                        ServiceGroupUserLeave.create());
                return getRouter().onNewMessage(group.peer(), message);
            }

            return Promise.success(null);
        });
    }

    @Verified
    public Promise<Void> onUserKicked(int groupId, long rid, int uid, int kicker, long date, boolean isSilent) {
        return forGroup(groupId, group -> {

            if (uid == myUid()) {
                // If kicked me, clear members and change member state
                groups().addOrUpdateItem(group
                        .clearMembers());
            } else {
                // else remove kicked user
                groups().addOrUpdateItem(group
                        .removeMember(uid));
            }

            // Create message if needed
            if (!isSilent) {
                Message message = new Message(rid, date, date, kicker,
                        kicker == myUid() ? MessageState.SENT : MessageState.UNKNOWN,
                        ServiceGroupUserKicked.create(uid));
                return getRouter().onNewMessage(group.peer(), message);
            }

            return Promise.success(null);
        });
    }

    @Verified
    public Promise<Void> onUserAdded(int groupId, long rid, int uid, int adder, long date, boolean isSilent) {
        return forGroup(groupId, group -> {

            groups().addOrUpdateItem(group.addMember(uid, adder, date));

            // Create message if needed
            if (!isSilent) {
                Message message = new Message(rid, date, date, adder,
                        adder == myUid() ? MessageState.SENT : MessageState.UNKNOWN,
                        ServiceGroupUserInvited.create(uid));
                return getRouter().onNewMessage(group.peer(), message);
            }
            return Promise.success(null);
        });
    }

    @Verified
    public Promise<Void> onTopicChanged(int groupId, String topic) {
        return forGroup(groupId, group -> {

            // Change group title
            Group upd = group.editTheme(topic);

            // Update group
            groups().addOrUpdateItem(upd);

            // Notify about group change
            return onGroupDescChanged(upd);
        });
    }

    @Verified
    public Promise<Void> onAboutChanged(int groupId, String about) {
        return forGroup(groupId, group -> {

            groups().addOrUpdateItem(group.editAbout(about));

            return Promise.success(null);
        });
    }


    @Verified
    public Promise<Void> onMembersUpdated(int groupId, List<ApiMember> members) {
        return forGroup(groupId, group -> {

            groups().addOrUpdateItem(group.updateMembers(members));

            return Promise.success(null);
        });
    }

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
                .flatMap(group -> {
                    if (!group.isHaveExtension()) {
                        ArrayList<ApiGroupOutPeer> groups = new ArrayList<>();
                        groups.add(new ApiGroupOutPeer(gid, group.getAccessHash()));
                        return api(new RequestLoadFullGroups(groups))
                                .map(r -> group.updateExt(r.getGroups().get(0)));
                    } else {
                        return Promise.failure(new RuntimeException("Already loaded"));
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
        } else if (update instanceof UpdateGroupCanSendMessagesChanged) {
            UpdateGroupCanSendMessagesChanged messagesChanged = (UpdateGroupCanSendMessagesChanged) update;
            return onCanWriteMessagesChanged(messagesChanged.getGroupId(), messagesChanged.canSendMessages());
        } else if (update instanceof UpdateGroupMemberChanged) {
            UpdateGroupMemberChanged memberChanged = (UpdateGroupMemberChanged) update;
            return onIsMemberChanged(memberChanged.getGroupId(), memberChanged.isMember());
        } else if (update instanceof UpdateGroupExtChanged) {
            UpdateGroupExtChanged extChanged = (UpdateGroupExtChanged) update;
            return onExtChanged(extChanged.getGroupId(), extChanged.getExt());
        }

        //
        // Members
        //
        else if (update instanceof UpdateGroupMembersUpdate) {
            UpdateGroupMembersUpdate membersUpdate = (UpdateGroupMembersUpdate) update;

        } else if (update instanceof UpdateGroupMemberAdminChanged) {
            UpdateGroupMemberAdminChanged adminChanged = (UpdateGroupMemberAdminChanged) update;

        } else if (update instanceof UpdateGroupMemberDiff) {
            UpdateGroupMemberDiff memberDiff = (UpdateGroupMemberDiff) update;

        } else if (update instanceof UpdateGroupMembersBecameAsync) {
            UpdateGroupMembersBecameAsync becameAsync = (UpdateGroupMembersBecameAsync) update;

        } else if (update instanceof UpdateGroupMembersCountChanged) {
            UpdateGroupMembersCountChanged membersCountChanged = (UpdateGroupMembersCountChanged) update;

        }

        //
        // Ext
        //

        else if (update instanceof UpdateGroupTopicChangedObsolete) {
            UpdateGroupTopicChangedObsolete topicChanged = (UpdateGroupTopicChangedObsolete) update;
            return onTopicChanged(topicChanged.getGroupId(), topicChanged.getTopic());
        } else if (update instanceof UpdateGroupAboutChangedObsolete) {
            UpdateGroupAboutChangedObsolete aboutChanged = (UpdateGroupAboutChangedObsolete) update;
            return onAboutChanged(aboutChanged.getGroupId(), aboutChanged.getAbout());
        } else if (update instanceof UpdateGroupInviteObsolete) {
            UpdateGroupInviteObsolete groupInvite = (UpdateGroupInviteObsolete) update;
            return onGroupInvite(groupInvite.getGroupId(),
                    groupInvite.getRid(), groupInvite.getInviteUid(), groupInvite.getDate(),
                    false);
        } else if (update instanceof UpdateGroupUserLeaveObsolete) {
            UpdateGroupUserLeaveObsolete leave = (UpdateGroupUserLeaveObsolete) update;
            return onUserLeave(leave.getGroupId(), leave.getRid(), leave.getUid(),
                    leave.getDate(), false);
        } else if (update instanceof UpdateGroupUserKickObsolete) {
            UpdateGroupUserKickObsolete userKick = (UpdateGroupUserKickObsolete) update;
            return onUserKicked(userKick.getGroupId(),
                    userKick.getRid(), userKick.getUid(), userKick.getKickerUid(), userKick.getDate(),
                    false);
        } else if (update instanceof UpdateGroupUserInvitedObsolete) {
            UpdateGroupUserInvitedObsolete userInvited = (UpdateGroupUserInvitedObsolete) update;
            return onUserAdded(userInvited.getGroupId(),
                    userInvited.getRid(), userInvited.getUid(), userInvited.getInviterUid(), userInvited.getDate(),
                    false);
        } else if (update instanceof UpdateGroupMembersUpdateObsolete) {
            return onMembersUpdated(((UpdateGroupMembersUpdateObsolete) update).getGroupId(),
                    ((UpdateGroupMembersUpdateObsolete) update).getMembers());
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
