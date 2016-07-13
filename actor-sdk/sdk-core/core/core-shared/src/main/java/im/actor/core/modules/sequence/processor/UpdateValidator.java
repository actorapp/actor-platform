package im.actor.core.modules.sequence.processor;

import java.util.Collection;
import java.util.HashSet;

import im.actor.core.api.ApiDialogGroup;
import im.actor.core.api.ApiDialogShort;
import im.actor.core.api.ApiMember;
import im.actor.core.api.ApiPeerType;
import im.actor.core.api.updates.UpdateChatGroupsChanged;
import im.actor.core.api.updates.UpdateContactRegistered;
import im.actor.core.api.updates.UpdateContactsAdded;
import im.actor.core.api.updates.UpdateContactsRemoved;
import im.actor.core.api.updates.UpdateGroupExtChanged;
import im.actor.core.api.updates.UpdateGroupFullExtChanged;
import im.actor.core.api.updates.UpdateGroupMemberAdminChanged;
import im.actor.core.api.updates.UpdateGroupMemberChanged;
import im.actor.core.api.updates.UpdateGroupMemberDiff;
import im.actor.core.api.updates.UpdateGroupMembersCountChanged;
import im.actor.core.api.updates.UpdateGroupMembersUpdated;
import im.actor.core.api.updates.UpdateGroupOwnerChanged;
import im.actor.core.api.updates.UpdateMessage;
import im.actor.core.api.updates.UpdateUserLocalNameChanged;
import im.actor.core.modules.AbsModule;
import im.actor.core.modules.ModuleContext;
import im.actor.core.network.parser.Update;
import im.actor.runtime.annotations.Verified;

public class UpdateValidator extends AbsModule {

    public UpdateValidator(ModuleContext context) {
        super(context);
    }

    public boolean isCausesInvalidation(Update update) {

        HashSet<Integer> users = new HashSet<>();
        HashSet<Integer> groups = new HashSet<>();

        if (update instanceof UpdateMessage) {
            UpdateMessage updateMessage = (UpdateMessage) update;
            users.add(updateMessage.getSenderUid());
            if (updateMessage.getPeer().getType() == ApiPeerType.GROUP) {
                groups.add(updateMessage.getPeer().getId());
            }
            if (updateMessage.getPeer().getType() == ApiPeerType.PRIVATE) {
                users.add(updateMessage.getPeer().getId());
            }
        } else if (update instanceof UpdateContactRegistered) {
            UpdateContactRegistered contactRegistered = (UpdateContactRegistered) update;
            users.add(contactRegistered.getUid());
        } else if (update instanceof UpdateContactsAdded) {
            users.addAll(((UpdateContactsAdded) update).getUids());
        } else if (update instanceof UpdateContactsRemoved) {
            users.addAll(((UpdateContactsRemoved) update).getUids());
        } else if (update instanceof UpdateUserLocalNameChanged) {
            UpdateUserLocalNameChanged localNameChanged = (UpdateUserLocalNameChanged) update;
            users.add(localNameChanged.getUid());
        } else if (update instanceof UpdateChatGroupsChanged) {
            UpdateChatGroupsChanged changed = (UpdateChatGroupsChanged) update;
            for (ApiDialogGroup group : changed.getDialogs()) {
                for (ApiDialogShort dialog : group.getDialogs()) {
                    if (dialog.getPeer().getType() == ApiPeerType.PRIVATE) {
                        users.add(dialog.getPeer().getId());
                    } else if (dialog.getPeer().getType() == ApiPeerType.GROUP) {
                        groups.add(dialog.getPeer().getId());
                    }
                }
            }
        } else if (update instanceof UpdateGroupMemberChanged) {
            UpdateGroupMemberChanged memberChanged = (UpdateGroupMemberChanged) update;
            groups.add(memberChanged.getGroupId());
        } else if (update instanceof UpdateGroupMemberDiff) {
            UpdateGroupMemberDiff diff = (UpdateGroupMemberDiff) update;
            groups.add(diff.getGroupId());
            for (Integer u : diff.getRemovedUsers()) {
                users.add(u);
            }
            for (ApiMember m : diff.getAddedMembers()) {
                users.add(m.getInviterUid());
                users.add(m.getUid());
            }
        } else if (update instanceof UpdateGroupMembersUpdated) {
            UpdateGroupMembersUpdated u = (UpdateGroupMembersUpdated) update;
            groups.add(u.getGroupId());
            for (ApiMember m : u.getMembers()) {
                users.add(m.getInviterUid());
                users.add(m.getUid());
            }
        } else if (update instanceof UpdateGroupMemberAdminChanged) {
            UpdateGroupMemberAdminChanged u = (UpdateGroupMemberAdminChanged) update;
            users.add(u.getUserId());
            groups.add(u.getGroupId());
        } else if (update instanceof UpdateGroupMembersCountChanged) {
            UpdateGroupMembersCountChanged countChanged = (UpdateGroupMembersCountChanged) update;
            groups.add(countChanged.getGroupId());
        } else if (update instanceof UpdateGroupOwnerChanged) {
            UpdateGroupOwnerChanged ownerChanged = (UpdateGroupOwnerChanged) update;
            groups.add(ownerChanged.getGroupId());
            users.add(ownerChanged.getUserId());
        } else if (update instanceof UpdateGroupFullExtChanged) {
            UpdateGroupFullExtChanged fullExtChanged = (UpdateGroupFullExtChanged) update;
            groups.add(fullExtChanged.getGroupId());
        } else if (update instanceof UpdateGroupExtChanged) {
            UpdateGroupExtChanged extChanged = (UpdateGroupExtChanged) update;
            groups.add(extChanged.getGroupId());
        }
        if (!hasUsers(users)) {
            return true;
        }

        if (!hasGroups(groups)) {
            return true;
        }

        return false;
    }

    @Verified
    public boolean hasUsers(Collection<Integer> uids) {
        for (Integer uid : uids) {
            if (users().getValue(uid) == null) {
                return false;
            }
        }
        return true;
    }

    @Verified
    public boolean hasGroups(Collection<Integer> gids) {
        for (Integer uid : gids) {
            if (groups().getValue(uid) == null) {
                return false;
            }
        }
        return true;
    }
}
