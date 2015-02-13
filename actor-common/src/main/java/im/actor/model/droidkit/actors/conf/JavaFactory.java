package im.actor.model.droidkit.actors.conf;

import im.actor.model.droidkit.actors.utils.AtomicIntegerCompat;
import im.actor.model.droidkit.actors.utils.AtomicLongCompat;
import im.actor.model.droidkit.actors.utils.ThreadLocalCompat;

/**
 * Created by ex3ndr on 09.02.15.
 */
public interface JavaFactory {

    long getCurrentTime();

    int getCoresCount();

    AtomicIntegerCompat createAtomicInt(int init);

    AtomicLongCompat createAtomicLong(long init);

    <T> ThreadLocalCompat<T> createThreadLocal();
}
