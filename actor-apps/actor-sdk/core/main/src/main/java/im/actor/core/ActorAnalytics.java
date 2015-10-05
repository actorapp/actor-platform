package im.actor.core;

import im.actor.core.analytics.Event;
import im.actor.core.analytics.Page;
import im.actor.runtime.Log;

public class ActorAnalytics {

    public ActorAnalytics(Messenger messenger) {

    }

    public void trackContentVisible(Page page) {
        Log.d("ActorAnalytics", "Content visible: " + page.getContentType() + " #" + page.getContentId());
    }

    public void trackContentHidden(Page page) {
        Log.d("ActorAnalytics", "Content hidden: " + page.getContentType() + " #" + page.getContentId());
    }

    public void trackEvent(Event event) {
        Log.d("ActorAnalytics", "Event: " + event.getActionType() + " #" + event.getActionId());
    }
}