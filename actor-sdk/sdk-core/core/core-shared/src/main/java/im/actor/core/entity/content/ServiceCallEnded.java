/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.core.entity.content;

import im.actor.core.api.ApiServiceExGroupCreated;
import im.actor.core.api.ApiServiceMessage;
import im.actor.core.entity.content.internal.ContentRemoteContainer;

public class ServiceCallEnded extends ServiceContent {


    public ServiceCallEnded(ContentRemoteContainer contentContainer) {
        super(contentContainer);
    }
}
