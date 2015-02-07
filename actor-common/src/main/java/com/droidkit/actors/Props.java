package com.droidkit.actors;

import com.droidkit.actors.mailbox.Mailbox;
import com.droidkit.actors.mailbox.MailboxesQueue;

/**
 * <p>Props is a configuration class to specify options for the creation of actors, think of it as an immutable and
 * thus freely shareable recipe for creating an actor including associated dispatcher information.</p>
 * For more information you may read about <a href="http://doc.akka.io/docs/akka/2.3.5/java/untyped-actors.html">Akka Props</a>.
 *
 * @author Steve Ex3NDR Korshakov (steve@actor.im)
 */
public final class Props<T extends Actor> {
    private static final int TYPE_DEFAULT = 1;
    private static final int TYPE_CREATOR = 2;

    private final Class<T> aClass;
    private final Object[] args;
    private final int type;
    private final ActorCreator<T> creator;
    private final MailboxCreator mailboxCreator;

    private final String dispatcher;

    private Props(Class<T> aClass, Object[] args, int type, String dispatcher, ActorCreator<T> creator,
                  MailboxCreator mailboxCreator) {
        this.aClass = aClass;
        this.args = args;
        this.type = type;
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
        if (type == TYPE_DEFAULT) {
            if (args == null || args.length == 0) {
                return aClass.newInstance();
            }
        } else if (type == TYPE_CREATOR) {
            return creator.create();
        }

        throw new RuntimeException("Unsupported create method");
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
        return new Props<T>(aClass, args, type, dispatcher, creator, mailboxCreator);
    }

    /**
     * Create props from class
     *
     * @param tClass Actor class
     * @param <T>    Actor class
     * @return Props object
     */
    public static <T extends Actor> Props<T> create(Class<T> tClass) {
        return new Props(tClass, null, TYPE_DEFAULT, null, null, null);
    }


    /**
     * Create props from class with custom mailbox
     *
     * @param tClass Actor class
     * @param <T>    Actor class
     * @return Props object
     */
    public static <T extends Actor> Props<T> create(Class<T> tClass, MailboxCreator mailboxCreator) {
        return new Props(tClass, null, TYPE_DEFAULT, null, null, mailboxCreator);
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
        return new Props<T>(clazz, null, TYPE_CREATOR, null, creator, null);
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
        return new Props<T>(clazz, null, TYPE_CREATOR, null, creator, mailboxCreator);
    }
}
