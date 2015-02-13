package im.actor.messenger.core.actors.auth;

import com.droidkit.actors.concurrency.Future;

import im.actor.api.scheme.AuthSession;
import im.actor.api.scheme.rpc.ResponseGetAuthSessions;

/**
 * Created by ex3ndr on 22.10.14.
 */
public interface AuthInt {
    public Future<ResponseGetAuthSessions> requestAuth();

    public Future<Boolean> removeAuth(AuthSession item);

    public Future<Boolean> removeAllAuth();
}
