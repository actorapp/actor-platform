package im.actor.model.modules.presence;

import im.actor.model.Messenger;
import im.actor.model.api.rpc.RequestSetOnline;
import im.actor.model.api.rpc.ResponseVoid;
import im.actor.model.droidkit.actors.Actor;
import im.actor.model.network.RpcCallback;
import im.actor.model.network.RpcException;

/**
 * Actor for processing current user's online status.
 * TODO: Implement correct request cancelling and timeout
 */
public class MyPresenceActor extends Actor {

    private static final int RESEND_TIMEOUT = 60 * 1000; // 1 min
    private static final int TIMEOUT = 90 * 1000;

    private boolean isVisible = false;

    private Messenger messenger;

    public MyPresenceActor(Messenger messenger) {
        this.messenger = messenger;
    }

    private void onAppVisible() {
        isVisible = true;
        self().sendOnce(new PerformOnline());
    }

    private void onAppHidden() {
        isVisible = false;
        self().sendOnce(new PerformOnline());
    }

    private void performOnline() {
        messenger.getActorApi().request(new RequestSetOnline(isVisible, TIMEOUT),
                new RpcCallback<ResponseVoid>() {
                    @Override
                    public void onResult(ResponseVoid response) {

                    }

                    @Override
                    public void onError(RpcException e) {

                    }
                });
        if (isVisible) {
            self().sendOnce(new PerformOnline(), RESEND_TIMEOUT);
        }
    }

    // Messages

    @Override
    public void onReceive(Object message) {
        if (message instanceof OnAppVisible) {
            onAppVisible();
        } else if (message instanceof OnAppHidden) {
            onAppHidden();
        } else if (message instanceof PerformOnline) {
            performOnline();
        } else {
            drop(message);
        }
    }

    public static class OnAppVisible {

    }

    public static class OnAppHidden {

    }

    public static class PerformOnline {

    }
}
