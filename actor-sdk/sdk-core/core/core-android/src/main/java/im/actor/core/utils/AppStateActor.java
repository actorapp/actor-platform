package im.actor.core.utils;

import im.actor.core.AndroidMessenger;
import im.actor.runtime.Log;
import im.actor.runtime.actors.Actor;
import im.actor.runtime.actors.Cancellable;

public class AppStateActor extends Actor {

    private static final String TAG = "AppStateActor";

    private static final int CLOSE_TIMEOUT = 100;

    private final AndroidMessenger messenger;

    private boolean isAppOpen = false;
    private boolean isScreenVisible = true;
    private int activityCount = 0;
    private Cancellable closeCancellable;

    public AppStateActor(AndroidMessenger messenger) {
        this.messenger = messenger;
    }

    private void onActivityOpened() {
        Log.d(TAG, "onActivityOpened");
        activityCount++;
        if (isScreenVisible) {
            onAppProbablyOpened();
        }
    }

    private void onActivityClosed() {
        Log.d(TAG, "onActivityClosed");
        activityCount--;

        if (activityCount == 0) {
            onAppProbablyClosed();
        }
    }

    private void onAppProbablyClosed() {
        Log.d(TAG, "onAppProbablyClosed");
        if (isAppOpen) {
            if (closeCancellable != null) {
                closeCancellable.cancel();
                closeCancellable = null;
            }
            closeCancellable = schedule(new MarkAppAsClosed(), CLOSE_TIMEOUT);
        }
    }

    private void onAppProbablyOpened() {
        Log.d(TAG, "onAppProbablyOpened");
        onAppOpened();
        if (closeCancellable != null) {
            closeCancellable.cancel();
            closeCancellable = null;
        }
    }

    private void onAppOpened() {
        Log.d(TAG, "onAppOpened");
        if (!isAppOpen) {
            isAppOpen = true;
            messenger.onAppVisible();
        }
    }

    private void onAppClosed() {
        Log.d(TAG, "onAppClosed");
        if (isAppOpen) {
            isAppOpen = false;
            messenger.onAppHidden();
        }
    }

    private void onScreenOn() {
        Log.d(TAG, "onScreenOn");
        isScreenVisible = true;
        if (activityCount > 0) {
            onAppProbablyOpened();
        }
    }

    public void onScreenOff() {
        Log.d(TAG, "onScreenOff");
        isScreenVisible = false;
        onAppProbablyClosed();
    }

    @Override
    public void onReceive(Object message) {
        if (message instanceof OnActivityOpened) {
            onActivityOpened();
        } else if (message instanceof OnActivityClosed) {
            onActivityClosed();
        } else if (message instanceof MarkAppAsClosed) {
            onAppClosed();
        } else if (message instanceof OnScreenOn) {
            onScreenOn();
        } else if (message instanceof OnScreenOff) {
            onScreenOff();
        } else {
            super.onReceive(message);
        }
    }

    private static class MarkAppAsClosed {

    }

    public static class OnActivityOpened {

    }

    public static class OnActivityClosed {

    }

    public static class OnScreenOn {

    }

    public static class OnScreenOff {

    }
}