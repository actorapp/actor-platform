/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.runtime.se;

import im.actor.runtime.generic.GenericThreadingProvider;
import im.actor.runtime.threading.Dispatcher;

public class JavaSeThreadingProvider extends GenericThreadingProvider {

    @Override
    public Dispatcher createDispatcher(String name) {
        return null;
    }
}
