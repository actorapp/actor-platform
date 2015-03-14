package im.actor.model.mvvm;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import im.actor.model.droidkit.actors.Actor;
import im.actor.model.droidkit.actors.ActorCreator;
import im.actor.model.droidkit.actors.ActorRef;
import im.actor.model.droidkit.actors.Props;

import static im.actor.model.droidkit.actors.ActorSystem.system;

/**
 * Created by ex3ndr on 14.03.15.
 */
public class DisplayList<T> {

    private static int NEXT_ID = 0;
    private final int DISPLAY_LIST_ID;
    private Hook<T> hook;
    private ActorRef executor;
    private ArrayList<T>[] lists;
    private volatile int currentList;

    private CopyOnWriteArrayList<Listener> listeners = new CopyOnWriteArrayList<Listener>();

    public DisplayList() {
        this(null, new ArrayList<T>());
    }

    public DisplayList(Hook<T> hook) {
        this(hook, new ArrayList<T>());
    }

    public DisplayList(Hook<T> hook, List<T> defaultValues) {
        this.DISPLAY_LIST_ID = NEXT_ID++;

        this.hook = hook;
        this.executor = system().actorOf(Props.create(ListSwitcher.class, new ActorCreator<ListSwitcher>() {
            @Override
            public ListSwitcher create() {
                return new ListSwitcher(DisplayList.this);
            }
        }), "display_lists/" + DISPLAY_LIST_ID);
        this.lists = new ArrayList[2];

        this.currentList = 0;
        this.lists[0] = new ArrayList<T>(defaultValues);
        this.lists[1] = new ArrayList<T>(defaultValues);
        if (hook != null) {
            hook.beforeDisplay(lists[0]);
        }
    }

    public int getSize() {
        MVVMEngine.checkMainThread();
        return lists[currentList].size();
    }

    public T getItem(int index) {
        MVVMEngine.checkMainThread();
        return lists[currentList].get(index);
    }

    public void editList(Modification<T> mod) {
        this.executor.send(new EditList<T>(mod));
    }

    public void addListener(Listener listener) {
        MVVMEngine.checkMainThread();
        if (!listeners.contains(listener)) {
            listeners.add(listener);
        }
    }

    public void removeListener(Listener listener) {
        MVVMEngine.checkMainThread();
        listeners.remove(listener);
    }

    public static interface Modification<T> {
        public void modify(List<T> sourceList);
    }

    public static interface Hook<T> {
        public void beforeDisplay(List<T> list);
    }

    // Update actor

    private static class ListSwitcher<T> extends Actor {
        private ArrayList<Modification<T>> pending = new ArrayList<Modification<T>>();
        private boolean isLocked;
        private DisplayList<T> displayList;

        private ListSwitcher(DisplayList<T> displayList) {
            this.displayList = displayList;
        }

        public void onEditList(final Modification<T> modification) {
            if (isLocked) {
                pending.add(modification);
                return;
            }

            ArrayList<T> backgroundList = displayList.lists[(displayList.currentList + 1) % 2];

            modification.modify(backgroundList);
            if (displayList.hook != null) {
                displayList.hook.beforeDisplay(backgroundList);
            }

            requestListSwitch(new Modification[]{modification});
        }

        private void requestListSwitch(final Modification<T>[] modifications) {
            isLocked = true;
            MVVMEngine.runOnUiThread(new Runnable() {
                @Override
                public void run() {

                    displayList.currentList = (displayList.currentList + 1) % 2;

                    for (Listener l : displayList.listeners) {
                        l.onCollectionChanged();
                    }

                    self().send(new ListSwitched<T>(modifications));
                }
            });
        }

        public void onListSwitched(Modification<T>[] modifications) {
            isLocked = false;

            ArrayList<T> backgroundList = displayList.lists[(displayList.currentList + 1) % 2];
            for (Modification m : modifications) {
                m.modify(backgroundList);
            }

            if (pending.size() > 0) {
                for (Modification m : pending) {
                    m.modify(backgroundList);
                }
                pending.clear();

                if (displayList.hook != null) {
                    displayList.hook.beforeDisplay(backgroundList);
                }
            }
        }

        @Override
        public void onReceive(Object message) {
            if (message instanceof ListSwitched) {
                onListSwitched(((ListSwitched) message).modifications);
            } else if (message instanceof EditList) {
                onEditList(((EditList) message).modification);
            } else {
                drop(message);
            }
        }
    }

    private static class ListSwitched<T> {
        private Modification<T>[] modifications;

        private ListSwitched(Modification<T>[] modifications) {
            this.modifications = modifications;
        }
    }

    private static class EditList<T> {
        private Modification<T> modification;

        private EditList(Modification<T> modification) {
            this.modification = modification;
        }
    }

    public interface Listener {
        public void onCollectionChanged();
    }
}
