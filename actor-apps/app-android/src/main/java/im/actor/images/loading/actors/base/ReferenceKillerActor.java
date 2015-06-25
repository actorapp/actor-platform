package im.actor.images.loading.actors.base;

import com.droidkit.actors.Actor;
import com.droidkit.actors.ActorSelection;
import com.droidkit.actors.Props;
import im.actor.images.cache.BitmapReference;

/**
 * Created by ex3ndr on 27.08.14.
 */
public class ReferenceKillerActor extends Actor {
    public static ActorSelection killer() {
        return new ActorSelection(Props.create(ReferenceKillerActor.class).changeDispatcher("ui"), "killer");
    }

    @Override
    public void onReceive(Object message) {
        if (message instanceof BitmapReference) {
            BitmapReference reference = ((BitmapReference) message);
            reference.release();
        }
    }
}
