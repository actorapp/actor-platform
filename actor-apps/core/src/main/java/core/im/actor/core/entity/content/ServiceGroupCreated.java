/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.core.entity.content;

import im.actor.core.api.ServiceExGroupCreated;
import im.actor.core.api.ServiceMessage;
import im.actor.core.entity.content.internal.ContentRemoteContainer;

public class ServiceGroupCreated extends ServiceContent {

    public static ServiceGroupCreated create() {
        return new ServiceGroupCreated(new ContentRemoteContainer(new ServiceMessage("Group created",
                new ServiceExGroupCreated())));
    }

    public ServiceGroupCreated(ContentRemoteContainer contentContainer) {
        super(contentContainer);
    }
}
