/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.runtime.js;

import com.google.gwt.user.client.Window;

import im.actor.runtime.LifecycleRuntime;
import im.actor.runtime.js.power.JsWakeLock;
import im.actor.runtime.power.WakeLock;

public class JsLifecycleProvider implements LifecycleRuntime {

    @Override
    public void killApp() {
        Window.Location.reload();
    }

    @Override
    public WakeLock makeWakeLock() {
        return new JsWakeLock();
    }
}
