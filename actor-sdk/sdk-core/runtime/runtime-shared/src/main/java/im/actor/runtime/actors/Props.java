/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.runtime.actors;

import im.actor.runtime.actors.mailbox.Mailbox;
import im.actor.runtime.actors.mailbox.MailboxesQueue;

/**
 * <p>Props is a configuration class to specify options for the creation of actors, think of it as an immutable and
 * thus freely shareable recipe for creating an actor including associated dispatcher information.</p>
 * For more information you may read about <a href="http://doc.akka.io/docs/akka/2.3.5/java/untyped-actors.html">Akka Props</a>.
 */
public final class Props<T extends Actor> {

    private final Class<T> aClass;
    private final Object[] args;
    private final ActorCreator<T> creator;
    private final MailboxCreator mailboxCreator;

    private final String dispatcher;

    private Props(Class<T> aClass, Object[] args, String dispatcher, ActorCreator<T> creator,
                  MailboxCreator mailboxCreator) {
        this.aClass = aClass;
        this.args = args;
        this.creator = creator;
        this.mailboxCreator = mailboxCreator;
        this.dispatcher = dispatcher;
    }

    /**
     * Creating actor from Props
     *
     * @return Actor
     * @throws Exception
     */
    public T create() throws Exception {
        return creator.create();
    }

    /**
     * Creating mailbox for actor
     *
     * @param queue queue of mailboxes
     * @return mailbox
     */
    public Mailbox createMailbox(MailboxesQueue queue) {
        if (mailboxCreator != null) {
            return mailboxCreator.createMailbox(queue);
        } else {
            return new Mailbox(queue);
        }
    }

    /**
     * Getting dispatcher id if available
     *
     * @return the dispatcher
     */
    public String getDispatcher() {
        return dispatcher;
    }

    /**
     * Changing dispatcher
     *
     * @param dispatcher dispatcher id
     * @return this
     */
    public Props<T> changeDispatcher(String dispatcher) {
        return new Props<T>(aClass, args, dispatcher, creator, mailboxCreator);
    }

    /**
     * Create props from Actor creator
     *
     * @param clazz   Actor class
     * @param creator Actor creator class
     * @param <T>     Actor class
     * @return Props object
     */
    public static <T extends Actor> Props<T> create(Class<T> clazz, ActorCreator<T> creator) {
        return new Props<T>(clazz, null, null, creator, null);
    }

    /**
     * Create props from Actor creator with custom mailbox
     *
     * @param clazz   Actor class
     * @param creator Actor creator class
     * @param <T>     Actor class
     * @return Props object
     */
    public static <T extends Actor> Props<T> create(Class<T> clazz, ActorCreator<T> creator, MailboxCreator mailboxCreator) {
        return new Props<T>(clazz, null, null, creator, mailboxCreator);
    }
}
