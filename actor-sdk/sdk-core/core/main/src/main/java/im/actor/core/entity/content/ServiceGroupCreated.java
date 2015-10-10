/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.core.entity.content;

import im.actor.core.api.ApiServiceExGroupCreated;
import im.actor.core.api.ApiServiceMessage;
import im.actor.core.entity.content.internal.ContentRemoteContainer;

public class ServiceGroupCreated extends ServiceContent {

    public static ServiceGroupCreated create() {
        return new ServiceGroupCreated(new ContentRemoteContainer(new ApiServiceMessage("Group created",
                new ApiServiceExGroupCreated())));
    }

    public ServiceGroupCreated(ContentRemoteContainer contentContainer) {
        super(contentContainer);
    }
}
