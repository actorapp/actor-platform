/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.core.entity.content;

import im.actor.core.api.ApiServiceExChangedAbout;
import im.actor.core.api.ApiServiceMessage;
import im.actor.core.entity.content.internal.ContentRemoteContainer;

public class ServiceGroupAboutChanged extends ServiceContent {

    public static ServiceGroupAboutChanged create(String title) {
        return new ServiceGroupAboutChanged(new ContentRemoteContainer(
                new ApiServiceMessage("About changed", new ApiServiceExChangedAbout(title))));
    }

    private String newAbout;

    public ServiceGroupAboutChanged(ContentRemoteContainer remoteContainer) {
        super(remoteContainer);
        ApiServiceMessage serviceMessage = (ApiServiceMessage) remoteContainer.getMessage();
        newAbout = ((ApiServiceExChangedAbout) serviceMessage.getExt()).getAbout();
    }

    public String getNewAbout() {
        return newAbout;
    }
}
