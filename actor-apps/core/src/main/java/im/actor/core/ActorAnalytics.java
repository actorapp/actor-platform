package im.actor.core;

import im.actor.core.analytics.ContentPage;
import im.actor.core.modules.internal.AnalyticsModule;

public class ActorAnalytics {

    private final AnalyticsModule context;

    public ActorAnalytics(Messenger messenger) {
        this.context = messenger.getModuleContext().getAnalyticsModule();
    }

    public void trackContentVisible(ContentPage contentPage) {

    }

    public void trackContentHidden(ContentPage contentPage) {

    }
}