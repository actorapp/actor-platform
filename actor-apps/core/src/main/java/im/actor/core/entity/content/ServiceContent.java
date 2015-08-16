/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.core.entity.content;

import im.actor.core.api.ApiServiceMessage;
import im.actor.core.entity.content.internal.ContentRemoteContainer;

public class ServiceContent extends AbsContent {

    private String compatText;

    public ServiceContent(ContentRemoteContainer contentContainer) {
        super(contentContainer);
        compatText = ((ApiServiceMessage) contentContainer.getMessage()).getText();
    }

    public String getCompatText() {
        return compatText;
    }
}