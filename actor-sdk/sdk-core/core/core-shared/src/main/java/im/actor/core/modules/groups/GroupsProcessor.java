/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.core.modules.groups;

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
import im.actor.core.modules.AbsModule;
import im.actor.core.modules.ModuleContext;
import im.actor.core.modules.sequence.processor.SequenceProcessor;
import im.actor.core.network.parser.Update;
import im.actor.runtime.actors.messages.Void;
import im.actor.runtime.promise.Promise;

public class GroupsProcessor extends AbsModule implements SequenceProcessor {

    public GroupsProcessor(ModuleContext context) {
        super(context);
    }

    @Override
    public Promise<Void> process(Update update) {
        if (update instanceof UpdateGroupTitleChanged ||
                update instanceof UpdateGroupMemberChanged ||
                update instanceof UpdateGroupAvatarChanged ||
                update instanceof UpdateGroupPermissionsChanged ||
                update instanceof UpdateGroupDeleted ||
                update instanceof UpdateGroupExtChanged ||

                update instanceof UpdateGroupMembersUpdated ||
                update instanceof UpdateGroupMemberAdminChanged ||
                update instanceof UpdateGroupMemberDiff ||
                update instanceof UpdateGroupMembersBecameAsync ||
                update instanceof UpdateGroupMembersCountChanged ||

                update instanceof UpdateGroupShortNameChanged ||
                update instanceof UpdateGroupAboutChanged ||
                update instanceof UpdateGroupTopicChanged ||
                update instanceof UpdateGroupOwnerChanged ||
                update instanceof UpdateGroupHistoryShared ||
                update instanceof UpdateGroupFullPermissionsChanged ||
                update instanceof UpdateGroupFullExtChanged) {
            return context().getGroupsModule().getRouter().onUpdate(update);
        }
        return null;
    }
}