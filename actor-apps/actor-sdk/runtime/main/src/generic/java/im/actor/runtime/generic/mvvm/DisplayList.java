/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.runtime.generic.mvvm;

import com.google.j2objc.annotations.ObjectiveCName;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import im.actor.runtime.generic.mvvm.alg.ChangeBuilder;
import im.actor.runtime.generic.mvvm.alg.Modification;
import im.actor.runtime.actors.Actor;
import im.actor.runtime.actors.ActorCreator;
import im.actor.runtime.actors.ActorRef;
import im.actor.runtime.actors.Props;
import im.actor.runtime.generic.mvvm.alg.Modifications;

import static im.actor.runtime.actors.ActorSystem.system;

public class DisplayList<T> {

    private static int NEXT_ID = 0;
    private final int DISPLAY_LIST_ID;
    private ActorRef executor;
    private ArrayList<T>[] lists;
    private volatile int currentList;
    private final OperationMode operationMode;
    private volatile Object processedList;

    private CopyOnWriteArrayList<Listener> listeners = new CopyOnWriteArrayList<Listener>();
    private CopyOnWriteArrayList<AndroidChangeListener<T>> androidListeners =
            new CopyOnWriteArrayList<AndroidChangeListener<T>>();
    private CopyOnWriteArrayList<AppleChangeListener<T>> appleListeners =
            new CopyOnWriteArrayList<AppleChangeListener<T>>();

    private ListProcessor<T> listProcessor = null;

    @ObjectiveCName("initWithMode:")
    public DisplayList(OperationMode operationMode) {
        this(operationMode, new ArrayList<T>());
    }

    @ObjectiveCName("initWithMode:withValues:")
    public DisplayList(OperationMode operationMode, List<T> defaultValues) {
        im.actor.runtime.Runtime.checkMainThread();

        this.DISPLAY_LIST_ID = NEXT_ID++;

        this.operationMode = operationMode;

        this.executor = system().actorOf(Props.create(ListSwitcher.class, new ActorCreator<ListSwitcher>() {
            @Override
            public ListSwitcher create() {
                return new ListSwitcher(DisplayList.this);
            }
        }).changeDispatcher("display_list"), "display_lists/" + DISPLAY_LIST_ID);
        this.lists = new ArrayList[2];

        this.currentList = 0;
        this.lists[0] = new ArrayList<T>(defaultValues);
        this.lists[1] = new ArrayList<T>(defaultValues);
    }

    @ObjectiveCName("size")
    public int getSize() {
        // im.actor.runtime.Runtime.checkMainThread();
        return lists[currentList].size();
    }

    @ObjectiveCName("itemWithIndex:")
    public T getItem(int index) {
        // im.actor.runtime.Runtime.checkMainThread();
        return lists[currentList].get(index);
    }

    @ObjectiveCName("editList:")
    public void editList(Modification<T> mod) {
        editList(mod, null);
    }

    @ObjectiveCName("editList:withCompletion:")
    public void editList(Modification<T> mod, Runnable executeAfter) {
        this.executor.send(new EditList<T>(mod, executeAfter, false));
    }

    @ObjectiveCName("editList:withCompletion:withLoadMoreFlag:")
    public void editList(Modification<T> mod, Runnable executeAfter, boolean isLoadMore) {
        this.executor.send(new EditList<T>(mod, executeAfter, isLoadMore));
    }

    @ObjectiveCName("editList:withLoadMoreFlag:")
    public void editList(Modification<T> mod, boolean isLoadMore) {
        this.executor.send(new EditList<T>(mod, null, isLoadMore));
    }

    @ObjectiveCName("forcePreprocessing")
    public void forcePreprocessing() {
        this.executor.send(new EditList<T>((Modification<T>) Modifications.noOp(), null, false));
    }

    @ObjectiveCName("setListProcessor:")
    public void setListProcessor(ListProcessor<T> listProcessor) {
        this.listProcessor = listProcessor;
    }

    @ObjectiveCName("getListProcessor")
    public ListProcessor<T> getListProcessor() {
        return listProcessor;
    }

    @ObjectiveCName("getProcessedList")
    public Object getProcessedList() {
        return processedList;
    }

    @ObjectiveCName("addListener:")
    public void addListener(Listener listener) {
        //im.actor.runtime.Runtime.checkMainThread();
        if (!listeners.contains(listener)) {
            listeners.add(listener);
        }
    }

    @ObjectiveCName("removeListener:")
    public void removeListener(Listener listener) {
        //im.actor.runtime.Runtime.checkMainThread();
        listeners.remove(listener);
    }

    @ObjectiveCName("addAndroidListener:")
    public void addAndroidListener(AndroidChangeListener<T> listener) {
        if (operationMode != OperationMode.ANDROID && operationMode != OperationMode.GENERAL) {
            throw new RuntimeException("Unable to set Android Listener in iOS mode");
        }
        //im.actor.runtime.Runtime.checkMainThread();

        if (!androidListeners.contains(listener)) {
            androidListeners.add(listener);
        }
    }

    @ObjectiveCName("removeAndroidListener:")
    public void removeAndroidListener(AndroidChangeListener<T> listener) {
        if (operationMode != OperationMode.ANDROID && operationMode != OperationMode.GENERAL) {
            throw new RuntimeException("Unable to set Android Listener in iOS mode");
        }
        //im.actor.runtime.Runtime.checkMainThread();

        androidListeners.remove(listener);
    }

    @ObjectiveCName("addAppleListener:")
    public void addAppleListener(AppleChangeListener<T> listener) {
        if (operationMode != OperationMode.IOS && operationMode != OperationMode.GENERAL) {
            throw new RuntimeException("Unable to set Android Listener in Android mode");
        }
        //im.actor.runtime.Runtime.checkMainThread();

        if (!appleListeners.contains(listener)) {
            appleListeners.add(listener);
        }
    }

    @ObjectiveCName("removeAppleListener:")
    public void removeAppleListener(AppleChangeListener<T> listener) {
        if (operationMode != OperationMode.IOS && operationMode != OperationMode.GENERAL) {
            throw new RuntimeException("Unable to set Android Listener in Android mode");
        }
        //im.actor.runtime.Runtime.checkMainThread();

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

        public void onEditList(final Modification<T> modification, final Runnable runnable, boolean isLoadMore) {

            if (modification != null) {
                pending.add(new ModificationHolder<T>(modification, runnable, isLoadMore));
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

            int count = 1;
            for (ModificationHolder h : pending) {
                if (h.isLoadMore) {
                    break;
                }
            }

            ModificationHolder<T>[] dest = new ModificationHolder[count];
            for (int i = 0; i < count; i++) {
                dest[i] = pending.remove(0);
            }

            ArrayList<ChangeDescription<T>> modRes = new ArrayList<ChangeDescription<T>>();

            for (ModificationHolder<T> m : dest) {
                List<ChangeDescription<T>> changes = m.modification.modify(backgroundList);
                modRes.addAll(changes);
            }

            // Build changes
            ArrayList<ChangeDescription<T>> androidChanges = null;
            AppleListUpdate appleChanges = null;
            if (displayList.operationMode == OperationMode.ANDROID
                    || displayList.operationMode == OperationMode.GENERAL) {
                androidChanges = ChangeBuilder.processAndroidModifications(modRes, initialList);
            }
            if (displayList.operationMode == OperationMode.IOS
                    || displayList.operationMode == OperationMode.GENERAL) {
                appleChanges = ChangeBuilder.processAppleModifications(modRes, initialList, dest[0].isLoadMore);
            }

            Object processedList = null;
            if (displayList.listProcessor != null) {
                processedList = displayList.listProcessor.process(backgroundList, displayList.processedList);
            }

            requestListSwitch(dest, initialList, androidChanges, appleChanges, dest[0].isLoadMore,
                    processedList);
        }

        private void requestListSwitch(final ModificationHolder<T>[] modifications,
                                       final ArrayList<T> initialList,
                                       final ArrayList<ChangeDescription<T>> androidChanges,
                                       final AppleListUpdate appleChanges,
                                       final boolean isLoadedMore,
                                       final Object processedList) {
            isLocked = true;
            im.actor.runtime.Runtime.postToMainThread(new Runnable() {
                @Override
                public void run() {

                    displayList.currentList = (displayList.currentList + 1) % 2;
                    displayList.processedList = processedList;

                    if (androidChanges != null) {
                        for (AndroidChangeListener<T> l : displayList.androidListeners) {
                            l.onCollectionChanged(new AndroidListUpdate<T>(initialList, androidChanges, isLoadedMore));
                        }
                    }

                    if (appleChanges != null) {
                        for (AppleChangeListener<T> l : displayList.appleListeners) {
                            l.onCollectionChanged(appleChanges);
                        }
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
                self().send(new EditList<T>(null, null, false));
            }
        }

        @Override
        public void onReceive(Object message) {
            if (message instanceof ListSwitched) {
                onListSwitched(((ListSwitched<T>) message).modifications);
            } else if (message instanceof EditList) {
                onEditList(((EditList<T>) message).modification, ((EditList) message).executeAfter,
                        ((EditList) message).isLoadMore);
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
        private boolean isLoadMore;

        private EditList(Modification<T> modification, Runnable executeAfter, boolean isLoadMore) {
            this.modification = modification;
            this.executeAfter = executeAfter;
            this.isLoadMore = isLoadMore;
        }
    }

    private static class ModificationHolder<T> {
        private Modification<T> modification;
        private Runnable executeAfter;
        private boolean isLoadMore;

        private ModificationHolder(Modification<T> modification, Runnable executeAfter, boolean isLoadMore) {
            this.modification = modification;
            this.executeAfter = executeAfter;
            this.isLoadMore = isLoadMore;
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
        void onCollectionChanged(AppleListUpdate modification);
    }

    public enum OperationMode {
        GENERAL, ANDROID, IOS
    }
}
