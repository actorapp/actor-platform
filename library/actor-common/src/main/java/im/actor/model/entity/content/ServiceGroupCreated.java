/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.model.entity.content;

import im.actor.model.api.ServiceExGroupCreated;
import im.actor.model.api.ServiceMessage;
import im.actor.model.entity.content.internal.ContentRemoteContainer;

public class ServiceGroupCreated extends ServiceContent {

    public static ServiceGroupCreated create() {
        return new ServiceGroupCreated(new ContentRemoteContainer(new ServiceMessage("Group created",
                new ServiceExGroupCreated())));
    }

    public ServiceGroupCreated(ContentRemoteContainer contentContainer) {
        super(contentContainer);
    }
}
