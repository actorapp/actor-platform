/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.runtime.actors;

/**
 * <p>Props is a configuration class to specify options for the creation of actors, think of it as an immutable and
 * thus freely shareable recipe for creating an actor including associated dispatcher information.</p>
 * For more information you may read about <a href="http://doc.akka.io/docs/akka/2.3.5/java/untyped-actors.html">Akka Props</a>.
 */
public final class Props {

    private final ActorCreator creator;

    private final String dispatcher;

    private final ActorSupervisor supervisor;

    private Props(String dispatcher, ActorCreator creator, ActorSupervisor supervisor) {
        this.creator = creator;
        this.dispatcher = dispatcher;
        this.supervisor = supervisor;
    }

    /**
     * Creating actor from Props
     *
     * @return Actor
     * @throws Exception
     */
    public Actor create() throws Exception {
        return creator.create();
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
     * Getting supervisor if available
     *
     * @return the supervisor
     */
    public ActorSupervisor getSupervisor() {
        return supervisor;
    }

    /**
     * Changing dispatcher
     *
     * @param dispatcher dispatcher id
     * @return updated props
     */
    public Props changeDispatcher(String dispatcher) {
        return new Props(dispatcher, creator, supervisor);
    }

    /**
     * Changing supervisor of an Actor
     *
     * @param supervisor supervisor of actor
     * @return updated props
     */
    public Props changeSupervisor(ActorSupervisor supervisor) {
        return new Props(dispatcher, creator, supervisor);
    }

    /**
     * Create props from Actor creator
     *
     * @param creator Actor creator class
     * @return Props object
     */
    public static Props create(ActorCreator creator) {
        return new Props(null, creator, null);
    }
}
