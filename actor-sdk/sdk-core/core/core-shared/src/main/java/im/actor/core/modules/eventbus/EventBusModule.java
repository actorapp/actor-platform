package im.actor.core.modules.eventbus;

import java.util.ArrayList;
import java.util.HashMap;

import im.actor.core.api.updates.UpdateEventBusDeviceConnected;
import im.actor.core.api.updates.UpdateEventBusDeviceDisconnected;
import im.actor.core.api.updates.UpdateEventBusDisposed;
import im.actor.core.api.updates.UpdateEventBusMessage;
import im.actor.core.modules.AbsModule;
import im.actor.core.modules.ModuleContext;
import im.actor.runtime.Log;
import im.actor.runtime.actors.ActorRef;

public class EventBusModule extends AbsModule {

    private HashMap<String, ActorRef> subscribers = new HashMap<>();
    private HashMap<String, ArrayList<Object>> pendingMessages = new HashMap<>();

    public EventBusModule(ModuleContext context) {
        super(context);
    }

    public void run() {

    }

    public synchronized void subscribe(String busId, ActorRef ref) {
        subscribers.put(busId, ref);
        if (pendingMessages.containsKey(busId)) {
            for (Object o : pendingMessages.get(busId)) {
                onEventBusUpdate(o);
            }
            pendingMessages.remove(busId);
        }
    }

    public synchronized void unsubscribe(String busId, ActorRef ref) {
        if (subscribers.get(busId) == ref) {
            subscribers.remove(busId);
        }
    }

    public synchronized void onEventBusUpdate(Object update) {
        if (update instanceof UpdateEventBusMessage) {
            UpdateEventBusMessage busMessage = (UpdateEventBusMessage) update;
            ActorRef dest = subscribers.get(busMessage.getId());
            if (dest != null) {
                dest.send(new EventBusActor.EventBusMessage(
                        busMessage.getSenderId(),
                        busMessage.getSenderDeviceId(),
                        busMessage.getMessage()));
                Log.d("EVENTBUS", "Delivered");
            } else {
                Log.d("EVENTBUS", "Not Delivered");
                if (!pendingMessages.containsKey(busMessage.getId())) {
                    pendingMessages.put(busMessage.getId(), new ArrayList<>());
                }
                pendingMessages.get(busMessage.getId()).add(update);
            }
        } else if (update instanceof UpdateEventBusDeviceConnected) {
            UpdateEventBusDeviceConnected deviceConnected = (UpdateEventBusDeviceConnected) update;
            ActorRef dest = subscribers.get(deviceConnected.getId());
            if (dest != null) {
                dest.send(new EventBusActor.EventBusDeviceConnected(
                        deviceConnected.getUserId(),
                        deviceConnected.getDeviceId()));
            } else {
                Log.d("EVENTBUS", "Not Delivered");
                if (!pendingMessages.containsKey(deviceConnected.getId())) {
                    pendingMessages.put(deviceConnected.getId(), new ArrayList<>());
                }
                pendingMessages.get(deviceConnected.getId()).add(update);
            }
        } else if (update instanceof UpdateEventBusDeviceDisconnected) {
            UpdateEventBusDeviceDisconnected deviceDisconnected = (UpdateEventBusDeviceDisconnected) update;
            ActorRef dest = subscribers.get(deviceDisconnected.getId());
            if (dest != null) {
                dest.send(new EventBusActor.EventBusDeviceDisconnected(deviceDisconnected.getUserId(),
                        deviceDisconnected.getDeviceId()));
            } else {
                Log.d("EVENTBUS", "Not Delivered");
                if (!pendingMessages.containsKey(deviceDisconnected.getId())) {
                    pendingMessages.put(deviceDisconnected.getId(), new ArrayList<>());
                }
                pendingMessages.get(deviceDisconnected.getId()).add(update);
            }
        } else if (update instanceof UpdateEventBusDisposed) {
            UpdateEventBusDisposed disposed = (UpdateEventBusDisposed) update;
            ActorRef dest = subscribers.get(disposed.getId());
            if (dest != null) {
                dest.send(new EventBusActor.EventBusDisposed());
            } else {
                Log.d("EVENTBUS", "Not Delivered");
                if (!pendingMessages.containsKey(disposed.getId())) {
                    pendingMessages.put(disposed.getId(), new ArrayList<>());
                }
                pendingMessages.get(disposed.getId()).add(update);
            }
        }
    }
}
