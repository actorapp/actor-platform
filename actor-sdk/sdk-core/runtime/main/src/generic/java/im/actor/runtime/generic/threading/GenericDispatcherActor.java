/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.runtime.generic.threading;

import im.actor.runtime.actors.ActorSystem;
import im.actor.runtime.actors.ThreadPriority;
import im.actor.runtime.actors.dispatch.Dispatch;
import im.actor.runtime.actors.mailbox.ActorDispatcher;
import im.actor.runtime.actors.mailbox.Envelope;
import im.actor.runtime.actors.mailbox.MailboxesQueue;

/**
 * Basic ActorDispatcher backed by ThreadPoolDispatcher
 */
public class GenericDispatcherActor extends ActorDispatcher {

    public GenericDispatcherActor(String name, ActorSystem actorSystem, int threadsCount, ThreadPriority priority) {
        super(name, actorSystem);
        initDispatcher(new GenericDispatcherThreads<Envelope, MailboxesQueue>(getName(), threadsCount, priority, new MailboxesQueue(),
                new Dispatch<Envelope>() {
                    @Override
                    public void dispatchMessage(Envelope message) {
                        processEnvelope(message);
                    }
                }, true));
    }
}
