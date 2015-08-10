/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.core.entity.content;

import im.actor.core.api.ServiceExContactRegistered;
import im.actor.core.api.ServiceMessage;
import im.actor.core.entity.content.internal.ContentRemoteContainer;

public class ServiceUserRegistered extends ServiceContent {

    public static ServiceUserRegistered create() {
        return new ServiceUserRegistered(new ContentRemoteContainer(new ServiceMessage("Contact registered",
                new ServiceExContactRegistered())));
    }

    public ServiceUserRegistered(ContentRemoteContainer contentContainer) {
        super(contentContainer);
    }
}
