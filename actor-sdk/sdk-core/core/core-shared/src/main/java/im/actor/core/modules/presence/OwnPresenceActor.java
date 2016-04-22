/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.core.modules.presence;

import im.actor.core.api.rpc.RequestSetOnline;
import im.actor.core.api.rpc.ResponseVoid;
import im.actor.core.modules.Modules;
import im.actor.core.events.AppVisibleChanged;
import im.actor.core.modules.ModuleActor;
import im.actor.core.network.RpcCallback;
import im.actor.core.network.RpcException;
import im.actor.runtime.actors.Cancellable;
import im.actor.runtime.eventbus.BusSubscriber;
import im.actor.runtime.eventbus.Event;

/**
 * Actor for processing current user's online status.
 * TODO: Implement correct request timeout
 */
public class OwnPresenceActor extends ModuleActor implements BusSubscriber {

    private static final int RESEND_TIMEOUT = 60 * 1000; // 1 min
    private static final int TIMEOUT = 90 * 1000;

    private boolean isAlwaysOnline = false;
    private boolean isVisible = false;
    private long prevRid = 0;
    private Cancellable performCancellable;

    public OwnPresenceActor(Modules messenger) {
        super(messenger);
    }

    @Override
    public void preStart() {
        // isAlwaysOnline = config().getDeviceCategory() == DeviceCategory.DESKTOP;
        isAlwaysOnline = false;

        if (isAlwaysOnline) {
            schedulePerform(0);
        } else {
            context().getEvents().subscribe(this, AppVisibleChanged.EVENT);
        }
    }

    private void onAppVisible() {
        isVisible = true;
        schedulePerform(0);
    }

    private void onAppHidden() {
        isVisible = false;
        schedulePerform(0);
    }

    private void performOnline() {
        if (prevRid != 0) {
            cancelRequest(prevRid);
            prevRid = 0;
        }
        boolean isOnline = isVisible || isAlwaysOnline;
        prevRid = request(new RequestSetOnline(isOnline, TIMEOUT, null, null),
                new RpcCallback<ResponseVoid>() {
                    @Override
                    public void onResult(ResponseVoid response) {

                    }

                    @Override
                    public void onError(RpcException e) {

                    }
                });
        if (isOnline) {
            schedulePerform(RESEND_TIMEOUT);
        }
    }

    private void schedulePerform(long delay) {
        if (performCancellable != null) {
            performCancellable.cancel();
            performCancellable = null;
        }
        performCancellable = schedule(new PerformOnline(), delay);
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
            super.onReceive(message);
        }
    }

    @Override
    public void onBusEvent(Event event) {
        if (event instanceof AppVisibleChanged) {
            AppVisibleChanged visibleChanged = (AppVisibleChanged) event;
            if (visibleChanged.isVisible()) {
                self().send(new OwnPresenceActor.OnAppVisible());
            } else {
                self().send(new OwnPresenceActor.OnAppHidden());
            }
        }
    }

    private static class OnAppVisible {

    }

    private static class OnAppHidden {

    }

    private static class PerformOnline {

    }
}
