package im.actor.core.entity.content;

import im.actor.core.api.ApiServiceMessage;
import im.actor.core.api.ApiServiceTimerChanged;
import im.actor.core.entity.content.internal.ContentRemoteContainer;

public class ServiceTimerChanged extends ServiceContent {

    public static ServiceTimerChanged create(int timer) {
        return new ServiceTimerChanged(new ContentRemoteContainer(
                new ApiServiceMessage("Timer changed", new ApiServiceTimerChanged(timer))));
    }

    private int timer;

    public ServiceTimerChanged(ContentRemoteContainer contentContainer) {
        super(contentContainer);

        ApiServiceMessage serviceMessage = (ApiServiceMessage) contentContainer.getMessage();
        timer = ((ApiServiceTimerChanged) serviceMessage.getExt()).getTimerMs();
    }

    public int getTimer() {
        return timer;
    }
}
