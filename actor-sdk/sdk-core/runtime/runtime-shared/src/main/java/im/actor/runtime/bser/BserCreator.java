/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.runtime.bser;

public interface BserCreator<T> {
    T createInstance();
}
