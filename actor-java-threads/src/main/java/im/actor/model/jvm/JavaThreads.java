package im.actor.model.jvm;

import com.droidkit.actors.ActorSystem;
import com.droidkit.actors.ThreadPriority;
import com.droidkit.actors.conf.DispatcherFactory;
import com.droidkit.actors.conf.EnvConfig;
import com.droidkit.actors.conf.JavaFactory;
import com.droidkit.actors.mailbox.ActorDispatcher;
import com.droidkit.actors.utils.AtomicIntegerCompat;
import com.droidkit.actors.utils.AtomicLongCompat;
import com.droidkit.actors.utils.ThreadLocalCompat;
import im.actor.model.jvm.actors.JavaDispatcher;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Created by ex3ndr on 06.02.15.
 */
public class JavaThreads {
    public static void init() {

        // Init Actors
        EnvConfig.setDispatcherFactory(new DispatcherFactory() {
            @Override
            public ActorDispatcher createDispatcher(String name, int threadsCount, ThreadPriority priority, ActorSystem actorSystem) {
                return new JavaDispatcher(name, actorSystem, threadsCount, priority);
            }
        });
        EnvConfig.setJavaFactory(new JavaFactory() {
            @Override
            public long getCurrentTime() {
                return System.nanoTime() / 1000000;
            }

            @Override
            public int getCoresCount() {
                return Runtime.getRuntime().availableProcessors();
            }

            @Override
            public AtomicIntegerCompat createAtomicInt(final int init) {
                return new AtomicIntegerCompat() {

                    private AtomicInteger atomicInteger = new AtomicInteger(init);

                    @Override
                    public int get() {
                        return atomicInteger.get();
                    }

                    @Override
                    public int incrementAndGet() {
                        return atomicInteger.incrementAndGet();
                    }

                    @Override
                    public int getAndIncrement() {
                        return atomicInteger.getAndIncrement();
                    }

                    @Override
                    public void compareAndSet(int exp, int v) {
                        atomicInteger.compareAndSet(exp, v);
                    }

                    @Override
                    public void set(int v) {
                        atomicInteger.set(v);
                    }
                };
            }

            @Override
            public AtomicLongCompat createAtomicLong(final long init) {
                return new AtomicLongCompat() {
                    AtomicLong atomicLong = new AtomicLong(init);

                    @Override
                    public long get() {
                        return atomicLong.get();
                    }

                    @Override
                    public long incrementAndGet() {
                        return atomicLong.incrementAndGet();
                    }

                    @Override
                    public long getAndIncrement() {
                        return atomicLong.getAndIncrement();
                    }

                    @Override
                    public void set(long v) {
                        atomicLong.set(v);
                    }
                };
            }

            @Override
            public <T> ThreadLocalCompat<T> createThreadLocal() {
                return new ThreadLocalCompat<T>() {

                    private ThreadLocal<T> tThreadLocal = new ThreadLocal<T>();

                    @Override
                    public T get() {
                        return tThreadLocal.get();
                    }

                    @Override
                    public void set(T v) {
                        tThreadLocal.set(v);
                    }

                    @Override
                    public void remove() {
                        tThreadLocal.remove();
                    }
                };
            }
        });
    }
}
