/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.model.modules;

import im.actor.model.droidkit.actors.ActorCreator;
import im.actor.model.droidkit.actors.ActorRef;
import im.actor.model.droidkit.actors.Props;
import im.actor.model.modules.avatar.OwnAvatarChangeActor;
import im.actor.model.viewmodel.OwnAvatarVM;

import static im.actor.model.droidkit.actors.ActorSystem.system;

public class Profile extends BaseModule {
    private ActorRef avatarChangeActor;
    private OwnAvatarVM ownAvatarVM;

    public Profile(final Modules modules) {
        super(modules);
        ownAvatarVM = new OwnAvatarVM();
        avatarChangeActor = system().actorOf(Props.create(OwnAvatarChangeActor.class, new ActorCreator<OwnAvatarChangeActor>() {
            @Override
             public OwnAvatarChangeActor create() {
                return new OwnAvatarChangeActor(modules);
            }
        }), "actor/avatar/my");
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
