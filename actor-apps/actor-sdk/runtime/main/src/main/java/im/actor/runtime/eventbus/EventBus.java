package im.actor.runtime.eventbus;

import java.util.ArrayList;
import java.util.HashMap;

import im.actor.runtime.Log;

/**
 * Event bus for dispatching events to various parts of application.
 * Not designed to heavy usage.
 */
public class EventBus {

    private HashMap<String, Event> stickyEvents = new HashMap<String, Event>();
    private HashMap<BusSubscriber, SubscriberConfig> subscribers = new HashMap<BusSubscriber, SubscriberConfig>();

    public synchronized void subscribe(BusSubscriber subscriber) {
        subscribe(subscriber, null);
    }

    public synchronized void subscribe(BusSubscriber subscriber, String eventType) {
        if (!subscribers.containsKey(subscriber)) {
            subscribers.put(subscriber, new SubscriberConfig(eventType));
        } else {
            SubscriberConfig config = subscribers.get(subscriber);
            if (!config.getTypes().contains(eventType)) {
                config.getTypes().add(eventType);
            }
        }

        if (eventType != null && stickyEvents.containsKey(eventType)) {
            subscriber.onBusEvent(stickyEvents.get(eventType));
        }
    }

    public synchronized void unsubscrive(BusSubscriber subscriber) {
        subscribers.remove(subscriber);
    }

    public void post(final Event event) {
        im.actor.runtime.Runtime.dispatch(new Runnable() {
            @Override
            public void run() {
                deliver(event, false);
            }
        });
    }

    public void postSticky(final Event event) {
        im.actor.runtime.Runtime.dispatch(new Runnable() {
            @Override
            public void run() {
                deliver(event, true);
            }
        });
    }

    private synchronized void deliver(Event e, boolean isSticky) {
        Log.d("EventBus", "Event: " + e);

        String eventType = e.getType();
        if (isSticky) {
            stickyEvents.put(eventType, e);
        }
        for (BusSubscriber s : subscribers.keySet()) {
            SubscriberConfig config = subscribers.get(s);
            if (config.getTypes().contains(null)
                    || config.getTypes().contains(eventType)) {
                s.onBusEvent(e);
            }
        }
    }

    private class SubscriberConfig {
        private ArrayList<String> types = new ArrayList<String>();

        public SubscriberConfig(String baseType) {
            this.types.add(baseType);
        }

        public ArrayList<String> getTypes() {
            return types;
        }
    }
}