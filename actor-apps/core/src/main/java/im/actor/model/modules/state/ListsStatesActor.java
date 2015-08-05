/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.model.modules.state;

import java.io.IOException;

import im.actor.model.api.AppCounters;
import im.actor.model.droidkit.bser.BserParser;
import im.actor.model.droidkit.bser.BserValues;
import im.actor.model.droidkit.bser.DataInput;
import im.actor.model.modules.Modules;
import im.actor.model.modules.utils.ModuleActor;

public class ListsStatesActor extends ModuleActor {

    private AppCounters counters;

    public ListsStatesActor(Modules modules) {
        super(modules);
    }

    @Override
    public void preStart() {
        super.preStart();

        counters = new AppCounters();
        byte[] data = preferences().getBytes("app.counter_raw");
        if (data != null) {
            try {
                AppCounters nCounters = new AppCounters();
                nCounters.parse(new BserValues(BserParser.deserialize(new DataInput(data))));
                counters = nCounters;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        Integer counter = counters.getGlobalCounter();
        if (counter != null) {
            modules().getAppStateModule().getAppStateVM().onGlobalCounterChanged(counter);
        } else {
            modules().getAppStateModule().getAppStateVM().onGlobalCounterChanged(0);
        }
    }

    public void onCounterChanged(AppCounters counters) {
        preferences().putBytes("app.counter_raw", counters.toByteArray());
        Integer counter = counters.getGlobalCounter();
        if (counter != null) {
            modules().getAppStateModule().getAppStateVM().onGlobalCounterChanged(counter);
        } else {
            modules().getAppStateModule().getAppStateVM().onGlobalCounterChanged(0);
        }
    }

    public void onDialogsChanged(boolean isEmpty) {
        modules().getAppStateModule().getAppStateVM().onDialogsChanged(isEmpty);
    }

    public void onContactsChanged(boolean isEmpty) {
        modules().getAppStateModule().getAppStateVM().onContactsChanged(isEmpty);
    }

    public void onBookImported() {
        modules().getAppStateModule().getAppStateVM().onPhoneImported();
    }

    public void onContactsLoaded() {
        modules().getAppStateModule().getAppStateVM().onContactsLoaded();
    }

    public void onDialogsLoaded() {
        modules().getAppStateModule().getAppStateVM().onDialogsLoaded();
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
        private AppCounters counters;

        public OnAppCounterChanged(AppCounters counters) {
            this.counters = counters;
        }

        public AppCounters getCounters() {
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
