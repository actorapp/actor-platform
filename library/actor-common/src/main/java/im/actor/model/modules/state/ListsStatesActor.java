package im.actor.model.modules.state;

import im.actor.model.modules.Modules;
import im.actor.model.modules.utils.ModuleActor;

/**
 * Created by ex3ndr on 28.03.15.
 */
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

    @Override
    public void onReceive(Object message) {
        if (message instanceof OnContactsChanged) {
            onContactsChanged(((OnContactsChanged) message).isEmpty());
        } else if (message instanceof OnDialogsChanged) {
            onDialogsChanged(((OnDialogsChanged) message).isEmpty());
        } else {
            drop(message);
        }
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
