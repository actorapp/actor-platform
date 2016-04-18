/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.core.entity.content;

import im.actor.core.api.ApiServiceExChangedTopic;
import im.actor.core.api.ApiServiceMessage;
import im.actor.core.entity.content.internal.ContentRemoteContainer;

public class ServiceGroupTopicChanged extends ServiceContent {

    public static ServiceGroupTopicChanged create(String topic) {
        return new ServiceGroupTopicChanged(new ContentRemoteContainer(
                new ApiServiceMessage("Topic changed", new ApiServiceExChangedTopic(topic))));
    }

    private String newTopic;

    public ServiceGroupTopicChanged(ContentRemoteContainer remoteContainer) {
        super(remoteContainer);
        ApiServiceMessage serviceMessage = (ApiServiceMessage) remoteContainer.getMessage();
        newTopic = ((ApiServiceExChangedTopic) serviceMessage.getExt()).getTopic();
    }

    public String getNewTopic() {
        return newTopic;
    }
}
