package com.droidkit.actors;

/**
 * Actor selection: props and path of actor
 *
 * @author Steve Ex3NDR Korshakov (steve@actor.im)
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
