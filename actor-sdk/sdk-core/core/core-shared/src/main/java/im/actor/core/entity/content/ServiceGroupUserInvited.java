/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.core.entity.content;

import im.actor.core.api.ApiServiceExUserInvited;
import im.actor.core.api.ApiServiceMessage;
import im.actor.core.entity.content.internal.ContentRemoteContainer;

public class ServiceGroupUserInvited extends ServiceContent {

    public static ServiceGroupUserInvited create(int uid) {
        return new ServiceGroupUserInvited(new ContentRemoteContainer(
                new ApiServiceMessage("User added", new ApiServiceExUserInvited(uid))));
    }

    private int addedUid;

    public ServiceGroupUserInvited(ContentRemoteContainer contentContainer) {
        super(contentContainer);
        ApiServiceMessage serviceMessage = (ApiServiceMessage) contentContainer.getMessage();
        addedUid = ((ApiServiceExUserInvited) serviceMessage.getExt()).getInvitedUid();
    }

    public int getAddedUid() {
        return addedUid;
    }
}
