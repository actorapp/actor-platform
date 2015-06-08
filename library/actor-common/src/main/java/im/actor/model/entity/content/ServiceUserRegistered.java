/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.model.entity.content;

import im.actor.model.api.ServiceExContactRegistered;
import im.actor.model.api.ServiceMessage;
import im.actor.model.entity.content.internal.ContentRemoteContainer;

public class ServiceUserRegistered extends ServiceContent {

    public static ServiceUserRegistered create() {
        return new ServiceUserRegistered(new ContentRemoteContainer(new ServiceMessage("Contact registered",
                new ServiceExContactRegistered())));
    }

    public ServiceUserRegistered(ContentRemoteContainer contentContainer) {
        super(contentContainer);
    }
}
