/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.core.modules.profile;

import im.actor.core.modules.AbsModule;
import im.actor.core.modules.Modules;
import im.actor.runtime.actors.ActorCreator;
import im.actor.runtime.actors.ActorRef;
import im.actor.runtime.actors.Props;
import im.actor.core.modules.profile.avatar.OwnAvatarChangeActor;
import im.actor.core.viewmodel.OwnAvatarVM;

import static im.actor.runtime.actors.ActorSystem.system;

public class ProfileModule extends AbsModule {

    private ActorRef avatarChangeActor;
    private OwnAvatarVM ownAvatarVM;

    public ProfileModule(final Modules modules) {
        super(modules);
        ownAvatarVM = new OwnAvatarVM();
        avatarChangeActor = system().actorOf("actor/avatar/my", () -> new OwnAvatarChangeActor(modules));
    }

    public OwnAvatarVM getOwnAvatarVM() {
        return ownAvatarVM;
    }

    public void changeAvatar(String descriptor) {
        avatarChangeActor.send(new OwnAvatarChangeActor.ChangeAvatar(descriptor));
    }

    public void removeAvatar() {
        avatarChangeActor.send(new OwnAvatarChangeActor.RemoveAvatar());
    }

    public void resetModule() {
        // TODO: Implement
    }
}
