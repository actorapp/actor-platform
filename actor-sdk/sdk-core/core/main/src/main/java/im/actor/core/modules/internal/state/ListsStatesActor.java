/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.core.modules.internal.state;

import java.io.IOException;

import im.actor.core.api.ApiAppCounters;
import im.actor.core.modules.ModuleContext;
import im.actor.core.modules.utils.ModuleActor;
import im.actor.runtime.bser.BserParser;
import im.actor.runtime.bser.BserValues;
import im.actor.runtime.bser.DataInput;

public class ListsStatesActor extends ModuleActor {

    private ApiAppCounters counters;

    public ListsStatesActor(ModuleContext context) {
        super(context);
    }

    @Override
    public void preStart() {
        super.preStart();

        counters = new ApiAppCounters();
        byte[] data = preferences().getBytes("app.counter_raw");
        if (data != null) {
            try {
                ApiAppCounters nCounters = new ApiAppCounters();
                nCounters.parse(new BserValues(BserParser.deserialize(new DataInput(data))));
                counters = nCounters;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        Integer counter = counters.getGlobalCounter();
        if (counter != null) {
            context().getAppStateModule().getAppStateVM().onGlobalCounterChanged(counter);
        } else {
            context().getAppStateModule().getAppStateVM().onGlobalCounterChanged(0);
        }
    }

    public void onCounterChanged(ApiAppCounters counters) {
        preferences().putBytes("app.counter_raw", counters.toByteArray());
        Integer counter = counters.getGlobalCounter();
        if (counter != null) {
            context().getAppStateModule().getAppStateVM().onGlobalCounterChanged(counter);
        } else {
            context().getAppStateModule().getAppStateVM().onGlobalCounterChanged(0);
        }
    }

    public void onDialogsChanged(boolean isEmpty) {
        context().getAppStateModule().getAppStateVM().onDialogsChanged(isEmpty);
    }

    public void onContactsChanged(boolean isEmpty) {
        context().getAppStateModule().getAppStateVM().onContactsChanged(isEmpty);
    }

    public void onBookImported() {
        context().getAppStateModule().getAppStateVM().onPhoneImported();
    }

    public void onContactsLoaded() {
        context().getAppStateModule().getAppStateVM().onContactsLoaded();
    }

    public void onDialogsLoaded() {
        context().getAppStateModule().getAppStateVM().onDialogsLoaded();
    }

    @Override
    public void onReceive(Object message) {
        if (message instanceof OnContactsChanged) {
            onContactsChanged(((OnContactsChanged) message).isEmpty());
        } else if (message instanceof OnDialogsChanged) {
            onDialogsChanged(((OnDialogsChanged) message).isEmpty());
        } else if (message instanceof OnBookImported) {
            onBookImported();
        } else if (message instanceof OnContactsLoaded) {
            onContactsLoaded();
        } else if (message instanceof OnDialogsLoaded) {
            onDialogsLoaded();
        } else if (message instanceof OnAppCounterChanged) {
            onCounterChanged(((OnAppCounterChanged) message).getCounters());
        } else {
            drop(message);
        }
    }

    public static class OnAppCounterChanged {
        private ApiAppCounters counters;

        public OnAppCounterChanged(ApiAppCounters counters) {
            this.counters = counters;
        }

        public ApiAppCounters getCounters() {
            return counters;
        }
    }

    public static class OnBookImported {

    }

    public static class OnContactsLoaded {

    }

    public static class OnDialogsLoaded {

    }

    public static class OnContactsChanged {
        private boolean isEmpty;

        public OnContactsChanged(boolean isEmpty) {
            this.isEmpty = isEmpty;
        }

        public boolean isEmpty() {
            return isEmpty;
        }
    }

    public static class OnDialogsChanged {
        private boolean isEmpty;

        public OnDialogsChanged(boolean isEmpty) {
            this.isEmpty = isEmpty;
        }

        public boolean isEmpty() {
            return isEmpty;
        }
    }
}
