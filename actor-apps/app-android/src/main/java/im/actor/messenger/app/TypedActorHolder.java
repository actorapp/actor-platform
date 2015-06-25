package im.actor.messenger.app;

import com.droidkit.actors.Actor;
import com.droidkit.actors.ActorRef;
import com.droidkit.actors.ActorSelection;
import com.droidkit.actors.Props;
import com.droidkit.actors.typed.TypedCreator;

import static com.droidkit.actors.ActorSystem.system;

/**
 * Created by ex3ndr on 08.10.14.
 */
public class TypedActorHolder<T> {
    private T res;
    private Class<T> clazz;
    private ActorSelection selection;
    private ActorRef ref;

    public <V extends Actor> TypedActorHolder(Class<T> clazz, Class<V> actor, String dispatcher, String path) {
        this(clazz, new ActorSelection(Props.create(actor).changeDispatcher(dispatcher), path));
    }

    public <V extends Actor> TypedActorHolder(Class<T> clazz, Class<V> actor, String path) {
        this(clazz, new ActorSelection(Props.create(actor), path));
    }

    public TypedActorHolder(Class<T> clazz, ActorSelection selection) {
        this.clazz = clazz;
        this.selection = selection;
        this.ref = system().actorOf(selection);
        this.res = TypedCreator.typed(ref, clazz);
    }

    public ActorRef getRef() {
        return ref;
    }

    public T get() {
        return res;
    }
}
