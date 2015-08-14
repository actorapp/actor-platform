/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.core.entity.content;

import org.jetbrains.annotations.Nullable;

import im.actor.core.api.ApiAvatar;
import im.actor.core.api.ApiServiceExChangedAvatar;
import im.actor.core.api.ApiServiceMessage;
import im.actor.core.entity.Avatar;
import im.actor.core.entity.content.internal.ContentRemoteContainer;

public class ServiceGroupAvatarChanged extends ServiceContent {

    public static ServiceGroupAvatarChanged create(ApiAvatar avatar) {
        return new ServiceGroupAvatarChanged(new ContentRemoteContainer(
                new ApiServiceMessage("Avatar changed", new ApiServiceExChangedAvatar(avatar))));
    }

    @Nullable
    private Avatar newAvatar;

    public ServiceGroupAvatarChanged(ContentRemoteContainer remoteContainer) {
        super(remoteContainer);

        ApiServiceMessage serviceMessage = (ApiServiceMessage) remoteContainer.getMessage();
        ApiServiceExChangedAvatar changedAvatar = ((ApiServiceExChangedAvatar) serviceMessage.getExt());
        newAvatar = (changedAvatar.getAvatar() != null) ? new Avatar(changedAvatar.getAvatar()) : null;
    }

    @Nullable
    public Avatar getNewAvatar() {
        return newAvatar;
    }
}
