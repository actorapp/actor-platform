/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.model.modules.state;

import im.actor.model.modules.Modules;
import im.actor.model.modules.utils.ModuleActor;

public class ListsStatesActor extends ModuleActor {

    public ListsStatesActor(Modules modules) {
        super(modules);
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
        } else {
            drop(message);
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
