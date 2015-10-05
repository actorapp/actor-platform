/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.core.modules.internal.presence;

import im.actor.core.api.rpc.RequestSetOnline;
import im.actor.core.api.rpc.ResponseVoid;
import im.actor.core.modules.Modules;
import im.actor.core.modules.events.AppVisibleChanged;
import im.actor.core.modules.utils.ModuleActor;
import im.actor.core.network.RpcCallback;
import im.actor.core.network.RpcException;
import im.actor.runtime.eventbus.BusSubscriber;
import im.actor.runtime.eventbus.Event;

/**
 * Actor for processing current user's online status.
 * TODO: Implement correct request cancelling and timeout
 */
public class OwnPresenceActor extends ModuleActor implements BusSubscriber {

    private static final int RESEND_TIMEOUT = 60 * 1000; // 1 min
    private static final int TIMEOUT = 90 * 1000;

    private boolean isVisible = false;

    public OwnPresenceActor(Modules messenger) {
        super(messenger);
    }

    @Override
    public void preStart() {
        context().getEvents().subscribe(this, AppVisibleChanged.EVENT);
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
        request(new RequestSetOnline(isVisible, TIMEOUT),
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
