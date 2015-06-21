/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.model.entity.content;

import im.actor.model.api.ServiceExUserInvited;
import im.actor.model.api.ServiceMessage;
import im.actor.model.entity.content.internal.ContentRemoteContainer;

public class ServiceGroupUserInvited extends ServiceContent {

    public static ServiceGroupUserInvited create(int uid) {
        return new ServiceGroupUserInvited(new ContentRemoteContainer(
                new ServiceMessage("User added", new ServiceExUserInvited(uid))));
    }

    private int addedUid;

    public ServiceGroupUserInvited(ContentRemoteContainer contentContainer) {
        super(contentContainer);
        ServiceMessage serviceMessage = (ServiceMessage) contentContainer.getMessage();
        addedUid = ((ServiceExUserInvited) serviceMessage.getExt()).getInvitedUid();
    }

    public int getAddedUid() {
        return addedUid;
    }
}
