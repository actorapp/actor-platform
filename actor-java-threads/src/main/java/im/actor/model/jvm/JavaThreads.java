package im.actor.model.jvm;

import com.droidkit.actors.ActorSystem;
import com.droidkit.actors.ThreadPriority;
import com.droidkit.actors.conf.DispatcherFactory;
import com.droidkit.actors.conf.EnvConfig;
import com.droidkit.actors.mailbox.ActorDispatcher;
import im.actor.model.jvm.actors.JavaDispatcher;

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
    }
}
