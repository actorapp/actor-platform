package im.actor.core.modules.sequence.processor;

import java.util.Collection;
import java.util.HashSet;

import im.actor.core.api.ApiDialogGroup;
import im.actor.core.api.ApiDialogShort;
import im.actor.core.api.ApiPeerType;
import im.actor.core.api.updates.UpdateChatGroupsChanged;
import im.actor.core.api.updates.UpdateContactRegistered;
import im.actor.core.api.updates.UpdateContactsAdded;
import im.actor.core.api.updates.UpdateContactsRemoved;
import im.actor.core.api.updates.UpdateGroupInvite;
import im.actor.core.api.updates.UpdateGroupUserInvited;
import im.actor.core.api.updates.UpdateGroupUserKick;
import im.actor.core.api.updates.UpdateGroupUserLeave;
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
        } else if (update instanceof UpdateGroupInvite) {
            UpdateGroupInvite groupInvite = (UpdateGroupInvite) update;
            users.add(groupInvite.getInviteUid());
            groups.add(groupInvite.getGroupId());
        } else if (update instanceof UpdateGroupUserInvited) {
            UpdateGroupUserInvited invited = (UpdateGroupUserInvited) update;
            users.add(invited.getInviterUid());
            users.add(invited.getUid());
            groups.add(invited.getGroupId());
        } else if (update instanceof UpdateGroupUserKick) {
            UpdateGroupUserKick kick = (UpdateGroupUserKick) update;
            users.add(kick.getKickerUid());
            users.add(kick.getUid());
            groups.add(kick.getGroupId());
        } else if (update instanceof UpdateGroupUserLeave) {
            UpdateGroupUserLeave leave = (UpdateGroupUserLeave) update;
            users.add(leave.getUid());
            groups.add(leave.getGroupId());
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
