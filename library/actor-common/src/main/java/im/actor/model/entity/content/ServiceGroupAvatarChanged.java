/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.model.entity.content;

import org.jetbrains.annotations.Nullable;

import im.actor.model.api.ServiceExChangedAvatar;
import im.actor.model.api.ServiceMessage;
import im.actor.model.entity.Avatar;
import im.actor.model.entity.content.internal.ContentRemoteContainer;

public class ServiceGroupAvatarChanged extends ServiceContent {

    public static ServiceGroupAvatarChanged create(im.actor.model.api.Avatar avatar) {
        return new ServiceGroupAvatarChanged(new ContentRemoteContainer(
                new ServiceMessage("Avatar changed", new ServiceExChangedAvatar(avatar))));
    }

    @Nullable
    private Avatar newAvatar;

    public ServiceGroupAvatarChanged(ContentRemoteContainer remoteContainer) {
        super(remoteContainer);

        ServiceMessage serviceMessage = (ServiceMessage) remoteContainer.getMessage();
        ServiceExChangedAvatar changedAvatar = ((ServiceExChangedAvatar) serviceMessage.getExt());
        newAvatar = (changedAvatar.getAvatar() != null) ? new Avatar(changedAvatar.getAvatar()) : null;
    }

    @Nullable
    public Avatar getNewAvatar() {
        return newAvatar;
    }
}
