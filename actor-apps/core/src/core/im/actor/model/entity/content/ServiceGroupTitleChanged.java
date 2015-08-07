/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.model.entity.content;

import im.actor.model.api.ServiceExChangedTitle;
import im.actor.model.api.ServiceMessage;
import im.actor.model.entity.content.internal.ContentRemoteContainer;

public class ServiceGroupTitleChanged extends ServiceContent {

    public static ServiceGroupTitleChanged create(String title) {
        return new ServiceGroupTitleChanged(new ContentRemoteContainer(
                new ServiceMessage("Title changed", new ServiceExChangedTitle(title))));
    }

    private String newTitle;

    public ServiceGroupTitleChanged(ContentRemoteContainer remoteContainer) {
        super(remoteContainer);
        ServiceMessage serviceMessage = (ServiceMessage) remoteContainer.getMessage();
        newTitle = ((ServiceExChangedTitle) serviceMessage.getExt()).getTitle();
    }

    public String getNewTitle() {
        return newTitle;
    }
}
