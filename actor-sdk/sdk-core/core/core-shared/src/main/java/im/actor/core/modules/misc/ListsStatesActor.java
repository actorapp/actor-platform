/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.core.modules.misc;

import im.actor.core.modules.ModuleContext;
import im.actor.core.modules.ModuleActor;

public class ListsStatesActor extends ModuleActor {

    public ListsStatesActor(ModuleContext context) {
        super(context);
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
        } else {
            super.onReceive(message);
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
