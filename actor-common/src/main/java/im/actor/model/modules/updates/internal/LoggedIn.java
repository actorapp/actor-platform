package im.actor.model.modules.updates.internal;

import im.actor.model.api.User;

/**
 * Created by ex3ndr on 19.02.15.
 */
public class LoggedIn extends InternalUpdate {
    private User user;

    public LoggedIn(User user) {
        this.user = user;
    }

    public User getUser() {
        return user;
    }
}
