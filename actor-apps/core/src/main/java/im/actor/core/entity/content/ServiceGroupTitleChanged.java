/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.core.entity.content;

import im.actor.core.api.ApiServiceExChangedTitle;
import im.actor.core.api.ApiServiceMessage;
import im.actor.core.entity.content.internal.ContentRemoteContainer;

public class ServiceGroupTitleChanged extends ServiceContent {

    public static ServiceGroupTitleChanged create(String title) {
        return new ServiceGroupTitleChanged(new ContentRemoteContainer(
                new ApiServiceMessage("Title changed", new ApiServiceExChangedTitle(title))));
    }

    private String newTitle;

    public ServiceGroupTitleChanged(ContentRemoteContainer remoteContainer) {
        super(remoteContainer);
        ApiServiceMessage serviceMessage = (ApiServiceMessage) remoteContainer.getMessage();
        newTitle = ((ApiServiceExChangedTitle) serviceMessage.getExt()).getTitle();
    }

    public String getNewTitle() {
        return newTitle;
    }
}
