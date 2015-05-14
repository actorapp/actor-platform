/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.model.mvvm;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import im.actor.model.droidkit.actors.Actor;
import im.actor.model.droidkit.actors.ActorCreator;
import im.actor.model.droidkit.actors.ActorRef;
import im.actor.model.droidkit.actors.Props;

import static im.actor.model.droidkit.actors.ActorSystem.system;

public class DisplayList<T> {

    private static int NEXT_ID = 0;
    private final int DISPLAY_LIST_ID;
    private final OperationMode operationMode;
    private ActorRef executor;
    private ArrayList<T>[] lists;
    private ArrayList<T> androidNotificationList;
    private volatile int currentList;

    private CopyOnWriteArrayList<Listener> listeners = new CopyOnWriteArrayList<Listener>();
    private CopyOnWriteArrayList<AndroidListener> androidListeners = new CopyOnWriteArrayList<AndroidListener>();

    public DisplayList(OperationMode operationMode) {
        this(operationMode, new ArrayList<T>());
    }

    public DisplayList(OperationMode operationMode, List<T> defaultValues) {
        MVVMEngine.checkMainThread();

        this.DISPLAY_LIST_ID = NEXT_ID++;
        this.operationMode = operationMode;

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
        if (operationMode == OperationMode.ANDROID) {
            this.androidNotificationList = new ArrayList<T>(defaultValues);
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
        editList(mod, null);
    }

    public void editList(Modification<T> mod, Runnable executeAfter) {
        this.executor.send(new EditList<T>(mod, executeAfter));
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

    // Update actor

    private static class ListSwitcher<T> extends Actor {
        private ArrayList<ModificationHolder<T>> pending = new ArrayList<ModificationHolder<T>>();
        private boolean isLocked;
        private DisplayList<T> displayList;

        private ListSwitcher(DisplayList<T> displayList) {
            this.displayList = displayList;
        }

//        private void applyModification(ModificationResult<T> res, ArrayList<T> list) {
//            for (ModificationResult.Operation<T> op : res.getOperations()) {
//                switch (op.type) {
//                    case ADD:
//                        list.add(op.index, op.item);
//                        break;
//                    case MOVE:
//                        T itm = list.remove(op.index);
//                        list.add(op.destIndex, itm);
//                        break;
//                    case REMOVE:
//                        list.remove(op.index);
//                        break;
//                    case UPDATE:
//                        list.remove(op.index);
//                        list.add(op.index, op.item);
//                        break;
//                }
//            }
//        }

        public void onEditList(final Modification<T> modification, final Runnable runnable) {
            ModificationHolder<T> holder = new ModificationHolder<T>(modification, runnable);
            if (isLocked) {
                pending.add(holder);
                return;
            }

            ArrayList<T> backgroundList = displayList.lists[(displayList.currentList + 1) % 2];

            ModificationResult<T> res = modification.modify(backgroundList);

            requestListSwitch(new ModificationHolder[]{holder});
        }

        private void requestListSwitch(final ModificationHolder<T>[] modifications) {
            isLocked = true;
            MVVMEngine.runOnUiThread(new Runnable() {
                @Override
                public void run() {

                    displayList.currentList = (displayList.currentList + 1) % 2;

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
                ModificationResult<T> res = m.modification.modify(backgroundList);
            }

            if (pending.size() > 0) {
                ModificationHolder[] dest = pending.toArray(new ModificationHolder[pending.size()]);
                pending.clear();

                for (ModificationHolder m : dest) {
                    ModificationResult<T> res = m.modification.modify(backgroundList);
                }

                requestListSwitch(dest);
            }
        }

        @Override
        public void onReceive(Object message) {
            if (message instanceof ListSwitched) {
                onListSwitched(((ListSwitched) message).modifications);
            } else if (message instanceof EditList) {
                onEditList(((EditList) message).modification, ((EditList) message).executeAfter);
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
        void onCollectionChanged();
    }

    public interface AndroidListener<T> {
        void onCollectionChanged(AndroidListChange<T> modification);
    }

    public enum OperationMode {
        GENERAL, ANDROID, IOS
    }

    public interface Modification<T> {
        ModificationResult<T> modify(ArrayList<T> sourceList);
    }

    public static final class ModificationResult<T> {

        private ArrayList<Operation<T>> operations = new ArrayList<Operation<T>>();

        public ModificationResult() {

        }

        public void appendOperation(Operation<T> operation) {
            operations.add(operation);
        }

        public void appendMove(int index, int destIndex) {
            operations.add(new Operation(OperationType.MOVE, index, destIndex, 1));
        }

        public void appendUpdate(int index, T item) {
            operations.add(new Operation<T>(OperationType.UPDATE, index, item));
        }

        public void appendAdd(int index, T item) {
            operations.add(new Operation<T>(OperationType.ADD, index, item));
        }

        public void appendRemove(int index, int len) {
            operations.add(new Operation(OperationType.REMOVE, index, len));
        }

        public ArrayList<Operation<T>> getOperations() {
            return operations;
        }

        public boolean isEmpty() {
            return operations.size() == 0;
        }

        public static class Operation<T> {
            private OperationType type;
            private int index;
            private int destIndex;
            private int length;
            private T item;

            public Operation(OperationType type, int index, T item) {
                this.type = type;
                this.index = index;
                this.item = item;
                this.length = 1;
            }

            public Operation(OperationType type, int index) {
                this.type = type;
                this.index = index;
                this.length = 1;
            }

            public Operation(OperationType type, int index, int destIndex, int length) {
                this.type = type;
                this.index = index;
                this.destIndex = destIndex;
                this.length = length;
            }

            public Operation(OperationType type, int index, int length) {
                this.type = type;
                this.index = index;
                this.length = length;
            }

            public OperationType getType() {
                return type;
            }

            public int getIndex() {
                return index;
            }

            public int getDestIndex() {
                return destIndex;
            }

            public int getLength() {
                return length;
            }

            public T getItem() {
                return item;
            }
        }

        public enum OperationType {
            ADD, REMOVE, UPDATE, MOVE
        }
    }
}
