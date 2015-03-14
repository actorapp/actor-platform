package com.droidkit.engine.uilist;

import com.droidkit.actors.Actor;
import com.droidkit.actors.ActorCreator;
import com.droidkit.actors.ActorRef;
import com.droidkit.actors.ActorSelection;
import com.droidkit.actors.Props;
import com.droidkit.engine._internal.RunnableActor;
import com.droidkit.engine._internal.Utils;

import java.util.ArrayList;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;

import im.actor.messenger.util.Logger;

import static com.droidkit.actors.ActorSystem.system;

/**
 * Created by ex3ndr on 01.09.14.
 */
public class UiList<V> {

    private static final AtomicInteger NEXT_ID = new AtomicInteger(1);

    private static final boolean ENABLE_LOG = false;

    private ArrayList<V>[] lists;
    private int currentList;

    private ActorRef listEditActor;
    private CopyOnWriteArrayList<UiListListener> listeners;
    private CopyOnWriteArrayList<UiListStateListener> exListeners;

    public UiList() {
        this.lists = new ArrayList[2];
        this.lists[0] = new ArrayList<V>();
        this.lists[1] = new ArrayList<V>();
        this.currentList = 0;
        this.listeners = new CopyOnWriteArrayList<UiListListener>();
        this.exListeners = new CopyOnWriteArrayList<UiListStateListener>();
        this.listEditActor = system().actorOf(list(this, NEXT_ID.getAndIncrement()));
    }

    public int getSize() {
        if (!Utils.isUIThread()) {
            throw new RuntimeException("Can read from list only in UI Thread");
        }
        return lists[currentList].size();
    }

    public V getItem(int index) {
        if (!Utils.isUIThread()) {
            throw new RuntimeException("Can read from list only in UI Thread");
        }
        return lists[currentList].get(index);
    }

    public void addListener(UiListListener listListener) {
//        if (!Utils.isUIThread()) {
//            throw new RuntimeException("Can read from list only in UI Thread");
//        }
        if (!listeners.contains(listListener)) {
            listeners.add(listListener);
        }
    }

    public void removeListener(UiListListener listListener) {
        if (!Utils.isUIThread()) {
            throw new RuntimeException("Can read from list only in UI Thread");
        }
        listeners.remove(listListener);
    }

    public void addExListener(UiListStateListener listListener) {
        if (!Utils.isUIThread()) {
            throw new RuntimeException("Can read from list only in UI Thread");
        }
        if (!exListeners.contains(listListener)) {
            exListeners.add(listListener);
        }
    }

    public void removeExListener(UiListStateListener listListener) {
        if (!Utils.isUIThread()) {
            throw new RuntimeException("Can read from list only in UI Thread");
        }
        exListeners.remove(listListener);
    }

    public void editList(ListModification<V> modification) {
        listEditActor.send(new UiListActor.EditList(System.currentTimeMillis(), modification, null));
    }

    public void setList(ListModification<V> modification) {

        if (!Utils.isUIThread()) {
            throw new RuntimeException("Can set list only in UI Thread");
        }

        for (UiListStateListener listListener : exListeners) {
            try {
                listListener.onListPreUpdated();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        modification.modify(lists[0], true);
        modification.modify(lists[1], true);

        for (UiListStateListener listListener : exListeners) {
            try {
                listListener.onListPostUpdated();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        for (UiListListener listListener : listeners) {
            try {
                listListener.onListUpdated();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void switchList() {
        if (!Utils.isUIThread()) {
            throw new RuntimeException("Can switch lists only in UI Thread");
        }

        for (UiListStateListener listListener : exListeners) {
            try {
                listListener.onListPreUpdated();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        currentList = (currentList + 1) % 2;

        for (UiListStateListener listListener : exListeners) {
            try {
                listListener.onListPostUpdated();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        for (UiListListener listListener : listeners) {
            try {
                listListener.onListUpdated();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private ArrayList<V> getBackground() {
        return lists[(currentList + 1) % 2];
    }

    private ActorSelection list(final UiList<V> list, int index) {
        return new ActorSelection(Props.create(UiListActor.class, new ActorCreator<UiListActor>() {
            @Override
            public UiListActor create() {
                return new UiListActor(list);

            }
        }), "ui_list_" + index);
    }

    private static class UiListActor<V> extends Actor {

        private UiList<V> list;
        private ActorRef uiActor;
        private ArrayList<ListModification> pending;
        private boolean isLocked;

        public UiListActor(UiList<V> list) {
            this.list = list;
            this.pending = new ArrayList<ListModification>();
        }

        @Override
        public void preStart() {
            uiActor = system().actorOf(Props.create(RunnableActor.class).changeDispatcher("ui"), "engines_notify");
        }

        @Override
        public void onReceive(Object message) {
            if (message instanceof EditList) {
                EditList editList = (EditList) message;
                onEdit(editList.startTime, editList.runnable, editList.modification);
            } else if (message instanceof ListSwitched) {
                ListSwitched listSwitched = (ListSwitched) message;
                listSwitched(listSwitched.startTime, listSwitched.runnable, listSwitched.modification);
            }
        }

        private void onEdit(final long switchStart, final Runnable runnable, final ListModification modification) {
            if (isLocked) {
                pending.add(modification);
                return;
            }
            isLocked = true;

            long start = System.currentTimeMillis();
            modification.modify(list.getBackground(), true);
            if (ENABLE_LOG) {
                Logger.d("UiList", "Modify first phase in " + (System.currentTimeMillis() - start) + " ms");
            }

            uiActor.send(new Runnable() {
                @Override
                public void run() {
                    list.switchList();
                    self().send(new ListSwitched(switchStart, runnable, modification));
                }
            });
        }

        private void listSwitched(long starttime, final Runnable runnable, ListModification[] modifications) {
            isLocked = false;

            long startM = System.currentTimeMillis();
            int index = 0;
            for (ListModification modification1 : modifications) {
                boolean isLast = false;
                if (pending.size() == 0) {
                    index++;
                    if (index == modifications.length) {
                        isLast = true;
                    }
                }
                modification1.modify(list.getBackground(), isLast);
            }
            if (ENABLE_LOG) {
                Logger.d("UiList", "Modify second phase in " + (System.currentTimeMillis() - startM) + " ms");

                Logger.d("UiList", "Switched in " + (System.currentTimeMillis() - starttime) + " ms");
            }

            if (pending.size() > 0) {
                isLocked = true;
                long startPost = System.currentTimeMillis();
                int index2 = 0;
                for (ListModification modification : pending) {
                    index2++;
                    modification.modify(list.getBackground(), index2 == pending.size());
                }
                final ListModification[] listModifications = pending.toArray(new ListModification[pending.size()]);
                pending.clear();
                if (ENABLE_LOG) {
                    Logger.d("UiList", "Modify second phase/2 in " + (System.currentTimeMillis() - startPost) + " ms");
                }
                final long start = System.currentTimeMillis();
                uiActor.send(new Runnable() {
                    @Override
                    public void run() {
                        list.switchList();
                        self().send(new ListSwitched(start, runnable, listModifications));
                    }
                });
            } else {
                if (runnable != null) {
                    runnable.run();
                }
            }
        }

        private static class EditList {
            ListModification modification;
            Runnable runnable;
            long startTime;

            public EditList(long startTime, ListModification modification, Runnable runnable) {
                this.startTime = startTime;
                this.modification = modification;
                this.runnable = runnable;
            }
        }

        private static class ListSwitched {
            ListModification[] modification;
            Runnable runnable;
            long startTime;

            private ListSwitched(long startTime, Runnable runnable, ListModification[] modification) {
                this.startTime = startTime;
                this.runnable = runnable;
                this.modification = modification;
            }

            private ListSwitched(long startTime, Runnable runnable, ListModification modification) {
                this.startTime = startTime;
                this.runnable = runnable;
                this.modification = new ListModification[]{modification};
            }
        }
    }

}