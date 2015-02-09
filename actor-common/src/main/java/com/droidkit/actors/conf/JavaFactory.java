package com.droidkit.actors.conf;

import com.droidkit.actors.utils.AtomicIntegerCompat;
import com.droidkit.actors.utils.AtomicLongCompat;
import com.droidkit.actors.utils.ThreadLocalCompat;

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
