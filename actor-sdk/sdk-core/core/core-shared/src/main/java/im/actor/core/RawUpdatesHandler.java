package im.actor.core;

import im.actor.core.api.updates.UpdateRawUpdate;
import im.actor.runtime.actors.Actor;

public abstract class RawUpdatesHandler extends Actor {

    protected abstract void onRawUpdate(UpdateRawUpdate update);

    @Override
    public void onReceive(Object message) {
        if (message instanceof UpdateRawUpdate) {
            onRawUpdate((UpdateRawUpdate) message);
        } else {
            drop(message);
        }
    }
}
