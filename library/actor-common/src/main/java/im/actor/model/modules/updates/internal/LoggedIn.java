package im.actor.model.modules.updates.internal;

import im.actor.model.api.rpc.ResponseAuth;

/**
 * Created by ex3ndr on 19.02.15.
 */
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
