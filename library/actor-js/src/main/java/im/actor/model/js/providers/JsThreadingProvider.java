package im.actor.model.js.providers;

import java.util.Date;

import im.actor.model.js.providers.threading.JsAtomicInteger;
import im.actor.model.js.providers.threading.JsAtomicLong;
import im.actor.model.js.providers.threading.JsDispatch;
import im.actor.model.js.providers.threading.JsThreadLocal;
import im.actor.model.ThreadingProvider;
import im.actor.model.droidkit.actors.ActorSystem;
import im.actor.model.droidkit.actors.ThreadPriority;
import im.actor.model.droidkit.actors.mailbox.ActorDispatcher;
import im.actor.model.util.AtomicIntegerCompat;
import im.actor.model.util.AtomicLongCompat;
import im.actor.model.util.ThreadLocalCompat;

/**
 * Created by ex3ndr on 21.02.15.
 */
public class JsThreadingProvider implements ThreadingProvider {
    @Override
    public long getActorTime() {
        // TODO: Better approach
        return getCurrentTime();
    }

    @Override
    public long getCurrentTime() {
        return new Date().getTime();
    }

    @Override
    public int getCoresCount() {
        return 1;
    }

    @Override
    public AtomicIntegerCompat createAtomicInt(int value) {
        return new JsAtomicInteger(value);
    }

    @Override
    public AtomicLongCompat createAtomicLong(long value) {
        return new JsAtomicLong(value);
    }

    @Override
    public <T> ThreadLocalCompat<T> createThreadLocal() {
        return new JsThreadLocal<T>();
    }

    @Override
    public ActorDispatcher createDispatcher(String name, int threadsCount, ThreadPriority priority, ActorSystem actorSystem) {
        return createDefaultDispatcher(name, priority, actorSystem);
    }

    @Override
    public ActorDispatcher createDefaultDispatcher(String name, ThreadPriority priority, ActorSystem system) {
        return new JsDispatch(name, system);
    }
}
