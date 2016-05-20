package im.actor.core.modules.users;

import im.actor.core.api.updates.UpdateContactRegistered;
import im.actor.core.api.updates.UpdateUserAboutChanged;
import im.actor.core.api.updates.UpdateUserAvatarChanged;
import im.actor.core.api.updates.UpdateUserBlocked;
import im.actor.core.api.updates.UpdateUserBotCommandsChanged;
import im.actor.core.api.updates.UpdateUserContactsChanged;
import im.actor.core.api.updates.UpdateUserExtChanged;
import im.actor.core.api.updates.UpdateUserFullExtChanged;
import im.actor.core.api.updates.UpdateUserLocalNameChanged;
import im.actor.core.api.updates.UpdateUserNameChanged;
import im.actor.core.api.updates.UpdateUserNickChanged;
import im.actor.core.api.updates.UpdateUserPreferredLanguagesChanged;
import im.actor.core.api.updates.UpdateUserTimeZoneChanged;
import im.actor.core.api.updates.UpdateUserUnblocked;
import im.actor.core.modules.AbsModule;
import im.actor.core.modules.ModuleContext;
import im.actor.core.modules.sequence.processor.SequenceProcessor;
import im.actor.core.network.parser.Update;
import im.actor.runtime.actors.messages.Void;
import im.actor.runtime.promise.Promise;

public class UsersProcessor extends AbsModule implements SequenceProcessor {

    public UsersProcessor(ModuleContext context) {
        super(context);
    }

    @Override
    public Promise<Void> process(Update update) {
        if (update instanceof UpdateUserNameChanged ||
                update instanceof UpdateUserLocalNameChanged ||
                update instanceof UpdateUserNickChanged ||
                update instanceof UpdateUserAboutChanged ||
                update instanceof UpdateUserAvatarChanged ||
                update instanceof UpdateContactRegistered ||
                update instanceof UpdateUserTimeZoneChanged ||
                update instanceof UpdateUserPreferredLanguagesChanged ||
                update instanceof UpdateUserExtChanged ||
                update instanceof UpdateUserFullExtChanged ||
                update instanceof UpdateUserBotCommandsChanged ||
                update instanceof UpdateUserContactsChanged ||
                update instanceof UpdateUserBlocked ||
                update instanceof UpdateUserUnblocked) {
            return context().getUsersModule().getUserRouter().onUpdate(update);
        }
        return null;
    }
}
