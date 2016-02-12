/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.runtime.cocoa;

import im.actor.runtime.cocoa.threading.CocoaDispatcher;
import im.actor.runtime.generic.GenericThreadingProvider;
import im.actor.runtime.threading.Dispatcher;

public class CocoaThreadingProvider extends GenericThreadingProvider {

    @Override
    public Dispatcher createDispatcher(String name) {
        return new CocoaDispatcher();
    }
}
