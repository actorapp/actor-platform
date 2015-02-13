package im.actor.messenger.core.actors.presence;

import com.droidkit.actors.Actor;
import com.droidkit.actors.ActorRef;
import com.droidkit.actors.ActorSystem;
import com.droidkit.actors.concurrency.FutureCallback;

import im.actor.api.scheme.rpc.ResponseVoid;
import im.actor.messenger.util.Logger;

import static im.actor.messenger.core.Core.requests;

/**
 * Created by ex3ndr on 06.09.14.
 */
public class MyPresenceActor extends Actor {

    public static ActorRef myPresence() {
        return ActorSystem.system().actorOf(MyPresenceActor.class, "presence");
    }

    private static final String TAG = "MyPresence";

    private static final int RESEND_TIMEOUT = 60 * 1000; // 1 min
    private static final int TIMEOUT = 90 * 1000;

    private boolean isAppOpened = false;
    // private Future future;

    @Override
    public void onReceive(Object message) {
        if (message instanceof OnAppOpened) {
            if (!isAppOpened) {
                isAppOpened = true;
                Logger.d(TAG, "Going online");
                self().sendOnce(new PerformPresence());
            }
        } else if (message instanceof OnAppClosed) {
            if (isAppOpened) {
                isAppOpened = false;
                Logger.d(TAG, "Going offline");
                self().sendOnce(new PerformPresence());
            }
        } else if (message instanceof PerformPresence) {
//            if (future != null) {
//                //future.cancel();
//            }
            // TODO: Fix request cancelling
            Logger.d(TAG, "Performing request. isOnline: " + isAppOpened);
            ask(requests().setOnline(isAppOpened, TIMEOUT, RESEND_TIMEOUT), new FutureCallback<ResponseVoid>() {
                @Override
                public void onResult(ResponseVoid result) {
                    Logger.d(TAG, "Request completed.");
                }

                @Override
                public void onError(Throwable throwable) {
                    Logger.d(TAG, "Request error.");
                }
            });
            self().sendOnce(new PerformPresence(), RESEND_TIMEOUT);
        }
    }

    public static class PerformPresence {

    }

    public static class OnAppOpened {

    }

    public static class OnAppClosed {

    }
}
