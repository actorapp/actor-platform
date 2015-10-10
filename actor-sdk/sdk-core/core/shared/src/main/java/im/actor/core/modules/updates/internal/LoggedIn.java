/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.core.modules.updates.internal;

import im.actor.core.api.rpc.ResponseAuth;

public class LoggedIn extends InternalUpdate {
    private ResponseAuth auth;
    private Runnable runnable;

    public LoggedIn(ResponseAuth auth, Runnable runnable) {
        this.auth = auth;
        this.runnable = runnable;
    }

    public ResponseAuth getAuth() {
        return auth;
    }

    public Runnable getRunnable() {
        return runnable;
    }
}
