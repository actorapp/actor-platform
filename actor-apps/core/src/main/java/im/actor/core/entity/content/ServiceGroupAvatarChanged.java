/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.core.entity.content;

import org.jetbrains.annotations.Nullable;

import im.actor.core.api.ApiAvatar;
import im.actor.core.api.ServiceExChangedAvatar;
import im.actor.core.api.ServiceMessage;
import im.actor.core.entity.Avatar;
import im.actor.core.entity.content.internal.ContentRemoteContainer;

public class ServiceGroupAvatarChanged extends ServiceContent {

    public static ServiceGroupAvatarChanged create(ApiAvatar avatar) {
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
