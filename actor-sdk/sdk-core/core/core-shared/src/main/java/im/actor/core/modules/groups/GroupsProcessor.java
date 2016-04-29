/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.core.modules.groups;

import im.actor.core.api.updates.UpdateGroupAboutChanged;
import im.actor.core.api.updates.UpdateGroupAvatarChanged;
import im.actor.core.api.updates.UpdateGroupInvite;
import im.actor.core.api.updates.UpdateGroupMembersUpdate;
import im.actor.core.api.updates.UpdateGroupTitleChanged;
import im.actor.core.api.updates.UpdateGroupTopicChanged;
import im.actor.core.api.updates.UpdateGroupUserInvited;
import im.actor.core.api.updates.UpdateGroupUserKick;
import im.actor.core.api.updates.UpdateGroupUserLeave;
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
                update instanceof UpdateGroupTopicChanged ||
                update instanceof UpdateGroupAboutChanged ||
                update instanceof UpdateGroupAvatarChanged ||
                update instanceof UpdateGroupInvite ||
                update instanceof UpdateGroupUserLeave ||
                update instanceof UpdateGroupUserKick ||
                update instanceof UpdateGroupUserInvited ||
                update instanceof UpdateGroupMembersUpdate) {
            return context().getGroupsModule().getRouter().onUpdate(update);
        }
        return null;
    }
}