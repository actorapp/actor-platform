/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.model;

import com.google.j2objc.annotations.ObjectiveCName;

public interface LifecycleProvider {
    @ObjectiveCName("killApp")
    void killApp();
}