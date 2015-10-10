/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.core.entity.content;

import im.actor.core.api.ApiServiceExContactRegistered;
import im.actor.core.api.ApiServiceMessage;
import im.actor.core.entity.content.internal.ContentRemoteContainer;

public class ServiceUserRegistered extends ServiceContent {

    public static ServiceUserRegistered create() {
        return new ServiceUserRegistered(new ContentRemoteContainer(new ApiServiceMessage("Contact registered",
                new ApiServiceExContactRegistered())));
    }

    public ServiceUserRegistered(ContentRemoteContainer contentContainer) {
        super(contentContainer);
    }
}
