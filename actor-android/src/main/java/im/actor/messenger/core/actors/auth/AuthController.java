package im.actor.messenger.core.actors.auth;

import com.droidkit.actors.concurrency.Future;
import com.droidkit.actors.concurrency.FutureCallback;
import com.droidkit.actors.typed.TypedActor;
import com.droidkit.actors.typed.TypedFuture;

import im.actor.api.scheme.AuthSession;
import im.actor.api.scheme.rpc.ResponseGetAuthSessions;
import im.actor.api.scheme.rpc.ResponseVoid;
import im.actor.messenger.core.actors.base.TypedActorHolder;

import static im.actor.messenger.core.Core.requests;

/**
 * Created by ex3ndr on 22.10.14.
 */
public class AuthController extends TypedActor<AuthInt> implements AuthInt {

    private static TypedActorHolder<AuthInt> HOLDER = new TypedActorHolder<AuthInt>(AuthInt.class,
            AuthController.class, "auth_controller");

    public static AuthInt authController() {
        return HOLDER.get();
    }

    public AuthController() {
        super(AuthInt.class);
    }

    @Override
    public Future<ResponseGetAuthSessions> requestAuth() {
        final TypedFuture<ResponseGetAuthSessions> res = future();
        ask(requests().getAuthSessions(15000), new FutureCallback<ResponseGetAuthSessions>() {
            @Override
            public void onResult(ResponseGetAuthSessions result) {
                res.doComplete(result);
            }

            @Override
            public void onError(Throwable throwable) {
                res.doError(throwable);
            }
        });
        return res;
    }

    @Override
    public Future<Boolean> removeAuth(AuthSession item) {
        final TypedFuture<Boolean> res = future();
        ask(requests().terminateSession(item.getId(), 15000), new FutureCallback<ResponseVoid>() {
            @Override
            public void onResult(ResponseVoid result) {
                res.doComplete(true);
            }

            @Override
            public void onError(Throwable throwable) {
                res.doError(throwable);
            }
        });
        return res;
    }

    @Override
    public Future<Boolean> removeAllAuth() {
        final TypedFuture<Boolean> res = future();
        ask(requests().terminateAllSessions(15000), new FutureCallback<ResponseVoid>() {
            @Override
            public void onResult(ResponseVoid result) {
                res.doComplete(true);
            }

            @Override
            public void onError(Throwable throwable) {
                res.doError(throwable);
            }
        });
        return res;
    }
}
