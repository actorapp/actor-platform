package im.actor.core.modules.eventbus;

import java.util.ArrayList;
import java.util.HashMap;

import im.actor.core.modules.AbsModule;
import im.actor.core.modules.ModuleContext;
import im.actor.runtime.actors.ActorRef;

public class EventBusModule extends AbsModule {

    private HashMap<String, ArrayList<ActorRef>> subscribers = new HashMap<>();

    public EventBusModule(ModuleContext context) {
        super(context);
    }

    public void run() {

    }

    public synchronized void subscribe(String busId, ActorRef ref) {
        if (!subscribers.containsKey(busId)) {
            subscribers.put(busId, new ArrayList<ActorRef>());
        }
        subscribers.get(busId).add(ref);
    }

    public synchronized void unsubscribe(String busId, ActorRef ref) {
        if (subscribers.containsKey(busId)) {
            subscribers.get(busId).remove(ref);
        }
    }

    public synchronized void onEventBusUpdate(Object update) {

    }
}
