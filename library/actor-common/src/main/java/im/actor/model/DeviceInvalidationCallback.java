/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.model;

import com.google.j2objc.annotations.ObjectiveCName;

/**
 * Callback is used when device is no longer logged in
 */
public interface DeviceInvalidationCallback {

    @ObjectiveCName("onAuthenticationInvalidated")
    void onAuthenticationInvalidated();
}