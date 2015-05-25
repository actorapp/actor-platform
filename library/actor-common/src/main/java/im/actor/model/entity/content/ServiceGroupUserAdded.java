/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.model.entity.content;

import im.actor.model.api.ServiceExUserAdded;
import im.actor.model.api.ServiceMessage;
import im.actor.model.entity.content.internal.ContentRemoteContainer;

public class ServiceGroupUserAdded extends ServiceContent {

    public static ServiceGroupUserAdded create(int uid) {
        return new ServiceGroupUserAdded(new ContentRemoteContainer(
                new ServiceMessage("User added", new ServiceExUserAdded(uid))));
    }

    private int addedUid;

    public ServiceGroupUserAdded(ContentRemoteContainer contentContainer) {
        super(contentContainer);
        ServiceMessage serviceMessage = (ServiceMessage) contentContainer.getMessage();
        addedUid = ((ServiceExUserAdded) serviceMessage.getExt()).getAddedUid();
    }

    public int getAddedUid() {
        return addedUid;
    }
}
