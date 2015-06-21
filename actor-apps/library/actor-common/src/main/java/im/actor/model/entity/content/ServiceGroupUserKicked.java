/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.model.entity.content;

import im.actor.model.api.ServiceExUserKicked;
import im.actor.model.api.ServiceMessage;
import im.actor.model.entity.content.internal.ContentRemoteContainer;

public class ServiceGroupUserKicked extends ServiceContent {

    public static ServiceGroupUserKicked create(int uid) {
        return new ServiceGroupUserKicked(new ContentRemoteContainer(
                new ServiceMessage("User kicked", new ServiceExUserKicked(uid))));
    }

    private int kickedUid;

    public ServiceGroupUserKicked(ContentRemoteContainer contentContainer) {
        super(contentContainer);

        ServiceMessage serviceMessage = (ServiceMessage) contentContainer.getMessage();
        kickedUid = ((ServiceExUserKicked) serviceMessage.getExt()).getKickedUid();
    }

    public int getKickedUid() {
        return kickedUid;
    }
}
