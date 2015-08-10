/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.runtime.js;

import com.google.gwt.user.client.Window;

import im.actor.runtime.LifecycleRuntime;

public class JsLifecycleProvider implements LifecycleRuntime {
    @Override
    public void killApp() {
        Window.Location.reload();
    }
}
