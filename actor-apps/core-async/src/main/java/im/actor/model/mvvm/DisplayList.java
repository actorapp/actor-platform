/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.model.mvvm;

import com.google.j2objc.annotations.ObjectiveCName;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import im.actor.model.droidkit.actors.Actor;
import im.actor.model.droidkit.actors.ActorCreator;
import im.actor.model.droidkit.actors.ActorRef;
import im.actor.model.droidkit.actors.Props;
import im.actor.model.mvvm.alg.ChangeBuilder;
import im.actor.model.mvvm.alg.Modification;

import static im.actor.model.droidkit.actors.ActorSystem.system;

public class DisplayList<T> {

    private static int NEXT_ID = 0;
    private final int DISPLAY_LIST_ID;
    private ActorRef executor;
    private ArrayList<T>[] lists;
    private volatile int currentList;

    private CopyOnWriteArrayList<Listener> listeners = new CopyOnWriteArrayList<Listener>();
    private CopyOnWriteArrayList<AndroidChangeListener<T>> androidListeners =
            new CopyOnWriteArrayList<AndroidChangeListener<T>>();
    private CopyOnWriteArrayList<AppleChangeListener<T>> appleListeners =
            new CopyOnWriteArrayList<AppleChangeListener<T>>();
    private BackgroundProcessor backgroundProcessor = null;

    @ObjectiveCName("init")
    public DisplayList() {
        this(new ArrayList<T>());
    }

    @ObjectiveCName("initWithValues:")
    public DisplayList(List<T> defaultValues) {
        MVVMEngine.checkMainThread();

        this.DISPLAY_LIST_ID = NEXT_ID++;

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
    }

    @ObjectiveCName("size")
    public int getSize() {
        MVVMEngine.checkMainThread();
        return lists[currentList].size();
    }

    @ObjectiveCName("itemWithIndex:")
    public T getItem(int index) {
        MVVMEngine.checkMainThread();
        return lists[currentList].get(index);
    }

    @ObjectiveCName("editList:")
    public void editList(Modification<T> mod) {
        editList(mod, null);
    }

    @ObjectiveCName("editList:withCompletion:")
    public void editList(Modification<T> mod, Runnable executeAfter) {
        this.executor.send(new EditList<T>(mod, executeAfter));
    }

    @ObjectiveCName("getBackgroundProcessor")
    public BackgroundProcessor getBackgroundProcessor() {
        return backgroundProcessor;
    }

    @ObjectiveCName("setBackgroundProcessor:")
    public void setBackgroundProcessor(BackgroundProcessor backgroundProcessor) {
        this.backgroundProcessor = backgroundProcessor;
    }

    @ObjectiveCName("addListener:")
    public void addListener(Listener listener) {
        MVVMEngine.checkMainThread();
        if (!listeners.contains(listener)) {
            listeners.add(listener);
        }
    }

    @ObjectiveCName("removeListener:")
    public void removeListener(Listener listener) {
        MVVMEngine.checkMainThread();
        listeners.remove(listener);
    }

    @ObjectiveCName("addAndroidListener:")
    public void addAndroidListener(AndroidChangeListener<T> listener) {
        MVVMEngine.checkMainThread();

        if (!androidListeners.contains(listener)) {
            androidListeners.add(listener);
        }
    }

    @ObjectiveCName("removeAndroidListener:")
    public void removeAndroidListener(AndroidChangeListener<T> listener) {
        MVVMEngine.checkMainThread();

        androidListeners.remove(listener);
    }

    @ObjectiveCName("addAppleListener:")
    public void addAppleListener(AppleChangeListener<T> listener) {
        MVVMEngine.checkMainThread();

        if (!appleListeners.contains(listener)) {
            appleListeners.add(listener);
        }
    }

    @ObjectiveCName("removeAppleListener:")
    public void removeAppleListener(AppleChangeListener<T> listener) {
        MVVMEngine.checkMainThread();

        appleListeners.remove(listener);
    }

    // Update actor

    private static class ListSwitcher<T> extends Actor {
        private ArrayList<ModificationHolder<T>> pending = new ArrayList<ModificationHolder<T>>();
        private boolean isLocked;
        private DisplayList<T> displayList;

        private ListSwitcher(DisplayList<T> displayList) {
            this.displayList = displayList;
        }

        public void onEditList(final Modification<T> modification, final Runnable runnable) {

            ModificationHolder<T> holder = new ModificationHolder<T>(modification, runnable);
            if (modification != null) {
                pending.add(holder);
            }

            if (isLocked) {
                return;
            }

            if (pending.size() == 0) {
                // Nothing to update
                return;
            }

            ArrayList<T> backgroundList = displayList.lists[(displayList.currentList + 1) % 2];
            ArrayList<T> initialList = new ArrayList<T>(backgroundList);

            ModificationHolder<T>[] dest = pending.toArray(new ModificationHolder[pending.size()]);
            pending.clear();
            ArrayList<ChangeDescription<T>> modRes = new ArrayList<ChangeDescription<T>>();

            for (ModificationHolder<T> m : dest) {
                List<ChangeDescription<T>> changes = m.modification.modify(backgroundList);
                if (displayList.backgroundProcessor != null) {
                    for (ChangeDescription<T> c : changes) {
                        if (c.getOperationType() == ChangeDescription.OperationType.ADD ||
                                c.getOperationType() == ChangeDescription.OperationType.UPDATE) {
                            for (T t : c.getItems()) {
                                displayList.backgroundProcessor.processInBackground(t);
                            }
                        }
                    }
                }
                modRes.addAll(changes);
            }

            // Build changes
            ArrayList<ChangeDescription<T>> androidChanges = ChangeBuilder.processAndroidModifications(modRes,
                    initialList);
            ArrayList<ChangeDescription<T>> appleChanges = ChangeBuilder.processAppleModifications(modRes,
                    initialList);

            requestListSwitch(dest, initialList, androidChanges, appleChanges);
        }

        private void requestListSwitch(final ModificationHolder<T>[] modifications,
                                       final ArrayList<T> initialList,
                                       final ArrayList<ChangeDescription<T>> androidChanges,
                                       final ArrayList<ChangeDescription<T>> appleChanges) {
            isLocked = true;
            MVVMEngine.runOnUiThread(new Runnable() {
                @Override
                public void run() {

                    displayList.currentList = (displayList.currentList + 1) % 2;

                    for (AndroidChangeListener<T> l : displayList.androidListeners) {
                        l.onCollectionChanged(new AndroidListUpdate<T>(initialList, androidChanges));
                    }

                    for (AppleChangeListener<T> l : displayList.appleListeners) {
                        l.onCollectionChanged(new AppleListUpdate<T>(appleChanges));
                    }

                    for (Listener l : displayList.listeners) {
                        l.onCollectionChanged();
                    }

                    for (ModificationHolder m : modifications) {
                        if (m.executeAfter != null) {
                            m.executeAfter.run();
                        }
                    }

                    self().send(new ListSwitched<T>(modifications));
                }
            });
        }

        public void onListSwitched(ModificationHolder<T>[] modifications) {
            isLocked = false;

            ArrayList<T> backgroundList = displayList.lists[(displayList.currentList + 1) % 2];
            for (ModificationHolder m : modifications) {
                m.modification.modify(backgroundList);
            }

            if (pending.size() > 0) {
                self().send(new EditList<T>(null, null));
            }
        }

        @Override
        public void onReceive(Object message) {
            if (message instanceof ListSwitched) {
                onListSwitched(((ListSwitched<T>) message).modifications);
            } else if (message instanceof EditList) {
                onEditList(((EditList<T>) message).modification, ((EditList) message).executeAfter);
            } else {
                drop(message);
            }
        }
    }

    private static class ListSwitched<T> {
        private ModificationHolder<T>[] modifications;

        private ListSwitched(ModificationHolder<T>[] modifications) {
            this.modifications = modifications;
        }
    }

    private static class EditList<T> {
        private Modification<T> modification;
        private Runnable executeAfter;

        private EditList(Modification<T> modification, Runnable executeAfter) {
            this.modification = modification;
            this.executeAfter = executeAfter;
        }
    }

    private static class ModificationHolder<T> {
        private Modification<T> modification;
        private Runnable executeAfter;

        private ModificationHolder(Modification<T> modification, Runnable executeAfter) {
            this.modification = modification;
            this.executeAfter = executeAfter;
        }
    }

    public interface Listener {
        @ObjectiveCName("onCollectionChanged")
        void onCollectionChanged();
    }

    public interface AndroidChangeListener<T> {
        @ObjectiveCName("onCollectionChangedWithChanges:")
        void onCollectionChanged(AndroidListUpdate<T> modification);
    }

    public interface AppleChangeListener<T> {
        @ObjectiveCName("onCollectionChangedWithChanges:")
        void onCollectionChanged(AppleListUpdate<T> modification);
    }

    public enum OperationMode {
        GENERAL, ANDROID, IOS
    }
}
