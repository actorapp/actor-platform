/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.core.js.providers;

import com.google.gwt.user.client.Window;

import im.actor.core.LifecycleProvider;

public class JsLifecycleProvider implements LifecycleProvider {
    @Override
    public void killApp() {
        Window.Location.reload();
    }
}
