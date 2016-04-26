package im.actor.core.modules.users;

import im.actor.core.api.updates.UpdateContactRegistered;
import im.actor.core.api.updates.UpdateUserAboutChanged;
import im.actor.core.api.updates.UpdateUserAvatarChanged;
import im.actor.core.api.updates.UpdateUserLocalNameChanged;
import im.actor.core.api.updates.UpdateUserNameChanged;
import im.actor.core.api.updates.UpdateUserNickChanged;
import im.actor.core.modules.AbsModule;
import im.actor.core.modules.ModuleContext;
import im.actor.core.modules.sequence.processor.SequenceProcessor;
import im.actor.core.modules.users.router.UserRouterInt;
import im.actor.core.network.parser.Update;
import im.actor.runtime.actors.messages.Void;
import im.actor.runtime.promise.Promise;

public class UsersProcessor extends AbsModule implements SequenceProcessor {

    public UsersProcessor(ModuleContext context) {
        super(context);
    }

    @Override
    public Promise<Void> process(Update update) {
        if (update instanceof UpdateUserNameChanged) {
            UpdateUserNameChanged userNameChanged = (UpdateUserNameChanged) update;
            return getRouter().onUserNameChanged(userNameChanged.getUid(), userNameChanged.getName());
        } else if (update instanceof UpdateUserLocalNameChanged) {
            UpdateUserLocalNameChanged localNameChanged = (UpdateUserLocalNameChanged) update;
            return getRouter().onUserLocalNameChanged(localNameChanged.getUid(), localNameChanged.getLocalName());
        } else if (update instanceof UpdateUserNickChanged) {
            UpdateUserNickChanged nickChanged = (UpdateUserNickChanged) update;
            return getRouter().onUserNicknameChanged(nickChanged.getUid(), nickChanged.getNickname());
        } else if (update instanceof UpdateUserAboutChanged) {
            UpdateUserAboutChanged userAboutChanged = (UpdateUserAboutChanged) update;
            return getRouter().onUserAboutChanged(userAboutChanged.getUid(), userAboutChanged.getAbout());
        } else if (update instanceof UpdateUserAvatarChanged) {
            UpdateUserAvatarChanged avatarChanged = (UpdateUserAvatarChanged) update;
            return getRouter().onUserAvatarChanged(avatarChanged.getUid(), avatarChanged.getAvatar());
        } else if (update instanceof UpdateContactRegistered) {
            UpdateContactRegistered registered = (UpdateContactRegistered) update;
            if (!registered.isSilent()) {
                return getRouter().onUserRegistered(registered.getUid(), registered.getRid(),
                        registered.getDate());
            }
            return Promise.success(null);
        }
        return null;
    }

    private UserRouterInt getRouter() {
        return context().getUsersModule().getUserRouter();
    }
}
