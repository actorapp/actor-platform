package im.actor.messenger.app;

import com.droidkit.actors.typed.TypedActor;

import im.actor.messenger.app.util.Logger;

/**
 * Created by ex3ndr on 06.09.14.
 */
public class AppStateBroker extends TypedActor<AppStateInterface> implements AppStateInterface {

    private static final TypedActorHolder<AppStateInterface> HOLDER = new TypedActorHolder<AppStateInterface>(AppStateInterface.class,
            AppStateBroker.class, "app_state");

    public static AppStateInterface stateBroker() {
        return HOLDER.get();
    }

    private static final int CLOSE_TIMEOUT = 1000;

    private boolean isAppOpen = false;
    private boolean isScreenVisible = true;

    private int activityCount = 0;

    private static final String TAG = "AppStateBroker";

    public AppStateBroker() {
        super(AppStateInterface.class);
    }

    @Override
    public void onReceive(Object message) {
        super.onReceive(message);
        if (message instanceof CloseApp) {
            if (isAppOpen) {
                isAppOpen = false;
                onAppClosed();
            }
        }
    }

    @Override
    public void onActivityOpen() {
        Logger.d(TAG, "Activity open");
        activityCount++;
        if (isScreenVisible) {
            onAppProbablyOpened();
        }
    }

    @Override
    public void onActivityClose() {
        Logger.d(TAG, "Activity close");
        activityCount--;

        if (activityCount == 0) {
            onAppProbablyClosed();
        }
    }

    @Override
    public void onScreenOn() {
        Logger.d(TAG, "Screen On");

        isScreenVisible = true;
        if (activityCount > 0) {
            onAppProbablyOpened();
        }
    }

    @Override
    public void onScreenOff() {
        Logger.d(TAG, "Screen Off");

        isScreenVisible = false;
        onAppProbablyClosed();
    }

    private void onAppProbablyClosed() {
        if (isAppOpen) {
            self().sendOnce(new CloseApp(), CLOSE_TIMEOUT);
        }
    }

    private void onAppProbablyOpened() {
        if (!isAppOpen) {
            isAppOpen = true;
            onAppOpened();
        }
        self().sendOnce(new CloseApp(), 24 * 60 * 60 * 1000); // Far away
    }

    private void onAppOpened() {
        Logger.d(TAG, "App open");
        Core.messenger().onAppVisible();
    }

    private void onAppClosed() {
        Logger.d(TAG, "App closed");
        Core.messenger().onAppHidden();
    }

    private static class CloseApp {

    }
}