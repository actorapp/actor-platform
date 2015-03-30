package im.actor.model.jvm.threads;

import im.actor.model.droidkit.actors.ActorSystem;
import im.actor.model.droidkit.actors.ThreadPriority;
import im.actor.model.droidkit.actors.dispatch.Dispatch;
import im.actor.model.droidkit.actors.mailbox.ActorDispatcher;
import im.actor.model.droidkit.actors.mailbox.Envelope;
import im.actor.model.droidkit.actors.mailbox.MailboxesQueue;

/**
 * Basic ActorDispatcher backed by ThreadPoolDispatcher
 *
 * @author Steve Ex3NDR Korshakov (steve@actor.im)
 */
public class JavaDispatcherActor extends ActorDispatcher {

    public JavaDispatcherActor(String name, ActorSystem actorSystem, int threadsCount, ThreadPriority priority) {
        super(name, actorSystem);
        initDispatcher(new JavaDispatcherThreads<Envelope, MailboxesQueue>(getName(), threadsCount, priority, new MailboxesQueue(),
                new Dispatch<Envelope>() {
                    @Override
                    public void dispatchMessage(Envelope message) {
                        processEnvelope(message);
                    }
                }, true));
    }
}
