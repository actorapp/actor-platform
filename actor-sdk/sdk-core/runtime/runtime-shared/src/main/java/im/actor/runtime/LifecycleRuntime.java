/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.runtime;

import com.google.j2objc.annotations.ObjectiveCName;

import im.actor.runtime.power.WakeLock;

public interface LifecycleRuntime {

    @ObjectiveCName("killApp")
    void killApp();

    @ObjectiveCName("makeWakeLock")
    WakeLock makeWakeLock();
}