/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.model.entity.content;

import im.actor.model.api.ServiceMessage;
import im.actor.model.entity.content.internal.ContentRemoteContainer;

public class ServiceContent extends AbsContent {

    private String compatText;

    public ServiceContent(ContentRemoteContainer contentContainer) {
        super(contentContainer);
        compatText = ((ServiceMessage) contentContainer.getMessage()).getText();
    }

    public String getCompatText() {
        return compatText;
    }
}