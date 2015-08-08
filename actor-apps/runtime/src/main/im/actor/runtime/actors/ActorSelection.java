/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.runtime.actors;

/**
 * Actor selection: props and path of actor
 */
public class ActorSelection {
    private final Props props;
    private final String path;

    public ActorSelection(Props props, String path) {
        this.props = props;
        this.path = path;
    }

    public Props getProps() {
        return props;
    }

    public String getPath() {
        return path;
    }
}
