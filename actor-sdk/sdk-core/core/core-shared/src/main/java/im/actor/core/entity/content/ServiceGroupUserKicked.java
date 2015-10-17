/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.core.entity.content;

import im.actor.core.api.ApiServiceExUserKicked;
import im.actor.core.api.ApiServiceMessage;
import im.actor.core.entity.content.internal.ContentRemoteContainer;

public class ServiceGroupUserKicked extends ServiceContent {

    public static ServiceGroupUserKicked create(int uid) {
        return new ServiceGroupUserKicked(new ContentRemoteContainer(
                new ApiServiceMessage("User kicked", new ApiServiceExUserKicked(uid))));
    }

    private int kickedUid;

    public ServiceGroupUserKicked(ContentRemoteContainer contentContainer) {
        super(contentContainer);

        ApiServiceMessage serviceMessage = (ApiServiceMessage) contentContainer.getMessage();
        kickedUid = ((ApiServiceExUserKicked) serviceMessage.getExt()).getKickedUid();
    }

    public int getKickedUid() {
        return kickedUid;
    }
}
