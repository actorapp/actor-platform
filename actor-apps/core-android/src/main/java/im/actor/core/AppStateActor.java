package im.actor.core;

import im.actor.runtime.actors.Actor;

/**
 * Created by ex3ndr on 31.07.15.
 */
public class AppStateActor extends Actor {

    private static final int CLOSE_TIMEOUT = 1000;

    private final AndroidMessenger messenger;

    private boolean isAppOpen = false;
    private boolean isScreenVisible = true;
    private int activityCount = 0;

    public AppStateActor(AndroidMessenger messenger) {
        this.messenger = messenger;
    }

    private void onActivityOpened() {
        activityCount++;
        if (isScreenVisible) {
            onAppProbablyOpened();
        }
    }

    private void onActivityClosed() {
        activityCount--;

        if (activityCount == 0) {
            onAppProbablyClosed();
        }
    }

    private void onAppProbablyClosed() {
        if (isAppOpen) {
            self().sendOnce(new MarkAppAsClosed(), CLOSE_TIMEOUT);
        }
    }

    private void onAppProbablyOpened() {
        if (!isAppOpen) {
            isAppOpen = true;
            onAppOpened();
        }
        self().sendOnce(new MarkAppAsClosed(), 24 * 60 * 60 * 1000); // Far away
    }

    private void onAppOpened() {
        messenger.onAppVisible();
    }

    private void onAppClosed() {
        messenger.onAppHidden();
    }

    private void onScreenOn() {
        isScreenVisible = true;
        if (activityCount > 0) {
            onAppProbablyOpened();
        }
    }

    public void onScreenOff() {
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