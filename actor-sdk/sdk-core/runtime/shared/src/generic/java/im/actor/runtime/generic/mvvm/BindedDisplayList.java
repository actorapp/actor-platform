/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.runtime.generic.mvvm;

import com.google.j2objc.annotations.ObjectiveCName;

import java.util.ArrayList;
import java.util.List;

import im.actor.runtime.generic.mvvm.alg.Modification;
import im.actor.runtime.generic.mvvm.alg.Modifications;
import im.actor.runtime.Log;
import im.actor.runtime.annotations.MainThread;
import im.actor.runtime.bser.BserObject;
import im.actor.runtime.mvvm.ValueModel;
import im.actor.runtime.storage.ListEngineDisplayExt;
import im.actor.runtime.storage.ListEngineDisplayListener;
import im.actor.runtime.storage.ListEngineDisplayLoadCallback;
import im.actor.runtime.storage.ListEngineItem;

public class BindedDisplayList<T extends BserObject & ListEngineItem> extends DisplayList<T>
        implements im.actor.runtime.mvvm.PlatformDisplayList<T> {

    private static final String TAG = "BindedDisplayList";

    private final ListEngineDisplayExt<T> listEngine;
    private final DisplayWindow window;
    private final EngineListener engineListener = new EngineListener();

    private int currentGeneration = 0;

    private final boolean isGlobalList;
    private final int pageSize;
    private final int loadGap;

    private BindHook<T> bindHook;

    private LinearLayoutCallback linearLayoutCallback;

    private ValueModel<State> stateModel;

    private ListMode mode;
    private String query;
    private boolean isLoadMoreForwardRequested = false;
    private boolean isLoadMoreBackwardRequested = false;
    private ArrayList<Modification<T>> pendingModifications = new ArrayList<Modification<T>>();

    public BindedDisplayList(ListEngineDisplayExt<T> listEngine, boolean isGlobalList,
                             int pageSize, int loadGap, OperationMode operationMode) {
        super(operationMode);

        this.isGlobalList = isGlobalList;
        this.pageSize = pageSize;
        this.loadGap = loadGap;

        this.listEngine = listEngine;
        this.window = new DisplayWindow();
        this.stateModel = new ValueModel<State>("display_list.state", State.LOADING_EMPTY);

        listEngine.subscribe(engineListener);
    }

    @ObjectiveCName("getBindHook")
    public BindHook<T> getBindHook() {
        return bindHook;
    }

    @ObjectiveCName("setBindHook:")
    public void setBindHook(BindHook<T> bindHook) {
        this.bindHook = bindHook;
    }

    @ObjectiveCName("isGlobalList")
    public boolean isGlobalList() {
        return isGlobalList;
    }

    @ObjectiveCName("isInSearchState")
    public boolean isInSearchState() {
        return mode == ListMode.SEARCH;
    }

    @MainThread
    @ObjectiveCName("touchWithIndex:")
    public void touch(int index) {
        im.actor.runtime.Runtime.checkMainThread();

        if (index >= getSize() - loadGap) {
            if (window.isForwardCompleted()) {
                if (bindHook != null) {
                    bindHook.onScrolledToEnd();
                }
            } else {
                loadMoreForward();
            }
        }

        if (index < loadGap) {
            loadMoreBackward();
        }

        if (bindHook != null) {
            bindHook.onItemTouched(getItem(index));
        }
    }

    // Init methods

    @Override
    public void initCenter(long rid) {
        initCenter(rid, false);
    }

    @Override
    public void initTop() {
        initTop(false);
    }

    @MainThread
    @ObjectiveCName("initEmpty")
    public void initEmpty() {
        im.actor.runtime.Runtime.checkMainThread();

        pendingModifications.clear();
        mode = ListMode.FORWARD;
        query = null;

        editList((Modification) Modifications.clear());

        stateModel.change(State.LOADING_EMPTY);
        currentGeneration++;
        window.emptyInit();
    }

    @MainThread
    @ObjectiveCName("initTopWithRefresh:")
    public void initTop(boolean refresh) {
        im.actor.runtime.Runtime.checkMainThread();

        // Log.d(TAG, "initTop(:" + refresh + ")");

        if (mode != null && mode == ListMode.FORWARD) {
            // Log.d(TAG, "Already loaded forward: exiting");
            return;
        }
        mode = ListMode.FORWARD;
        query = null;

        if (refresh) {
            editList((Modification) Modifications.clear());
        }

        stateModel.change(State.LOADING_EMPTY);
        currentGeneration++;
        window.startInitForward();
        pendingModifications.clear();

        listEngine.loadForward(pageSize, cover(new ListEngineDisplayLoadCallback<T>() {
            @Override
            public void onLoaded(List<T> items, long topSortKey, long bottomSortKey) {
                im.actor.runtime.Runtime.checkMainThread();

                window.completeInitForward(bottomSortKey);

                if (items.size() != 0) {
                    editList(Modifications.replace(items), true);
                } else {
                    window.onForwardCompleted();
                }
                for (Modification<T> m : pendingModifications) {
                    editList(m);
                }
                pendingModifications.clear();
            }
        }, currentGeneration));
    }

    @MainThread
    @ObjectiveCName("initBottomWithRefresh:")
    public void initBottom(boolean refresh) {
        im.actor.runtime.Runtime.checkMainThread();

        if (isGlobalList) {
            throw new RuntimeException("Global DisplayList can't grow from bottom");
        }

        if (mode != null && mode == ListMode.BACKWARD) {
            return;
        }
        mode = ListMode.BACKWARD;

        if (refresh) {
            editList((Modification) Modifications.clear(), true);
        }

        stateModel.change(State.LOADING_EMPTY);
        isLoadMoreBackwardRequested = false;
        isLoadMoreBackwardRequested = false;
        currentGeneration++;
        window.startInitBackward();
        pendingModifications.clear();

        listEngine.loadBackward(pageSize, cover(new ListEngineDisplayLoadCallback<T>() {
            @Override
            public void onLoaded(List<T> items, long topSortKey, long bottomSortKey) {
                im.actor.runtime.Runtime.checkMainThread();

                window.completeInitBackward(topSortKey);

                if (items.size() != 0) {
                    editList(Modifications.replace(items), true);
                } else {
                    window.onBackwardCompleted();
                }
                for (Modification<T> m : pendingModifications) {
                    editList(m);
                }
                pendingModifications.clear();
            }
        }, currentGeneration));
    }

    @MainThread
    @ObjectiveCName("initCenterWithKey:withRefresh:")
    public void initCenter(long centerSortKey, boolean refresh) {
        im.actor.runtime.Runtime.checkMainThread();

        if (mode != null && mode == ListMode.CENTER) {
            return;
        }
        mode = ListMode.CENTER;

        if (refresh) {
            editList((Modification) Modifications.clear(), true);
        }

        stateModel.change(State.LOADING_EMPTY);
        isLoadMoreBackwardRequested = false;
        isLoadMoreBackwardRequested = false;
        currentGeneration++;
        window.startInitCenter();
        pendingModifications.clear();

        listEngine.loadCenter(centerSortKey, pageSize, cover(new ListEngineDisplayLoadCallback<T>() {
            @Override
            public void onLoaded(List<T> items, long topSortKey, long bottomSortKey) {
                im.actor.runtime.Runtime.checkMainThread();

                window.completeInitCenter(bottomSortKey, topSortKey);

                if (items.size() != 0) {
                    editList(Modifications.addOrUpdate(items), true);
                } else {
                    window.onForwardCompleted();
                    window.onBackwardCompleted();
                }
                for (Modification<T> m : pendingModifications) {
                    editList(m);
                }
                pendingModifications.clear();
            }
        }, currentGeneration));
    }

    @MainThread
    @ObjectiveCName("initSearchWithQuery:withRefresh:")
    public void initSearch(String query, boolean refresh) {
        im.actor.runtime.Runtime.checkMainThread();

        if (isGlobalList) {
            throw new RuntimeException("Global DisplayList can't perform search");
        }

        if (query == null || query.trim().length() == 0) {
            throw new RuntimeException("Query can't be null or empty");
        }

        if (mode != null && mode == ListMode.SEARCH && this.query.equals(query)) {
            return;
        }
        this.mode = ListMode.SEARCH;
        this.query = query;

        if (refresh) {
            editList((Modification) Modifications.clear(), true);
        }

        stateModel.change(State.LOADING_EMPTY);
        isLoadMoreBackwardRequested = false;
        isLoadMoreBackwardRequested = false;
        currentGeneration++;
        window.startInitForward();
        pendingModifications.clear();

        listEngine.loadForward(query, pageSize, cover(new ListEngineDisplayLoadCallback<T>() {
            @Override
            public void onLoaded(List<T> items, long topSortKey, long bottomSortKey) {
                im.actor.runtime.Runtime.checkMainThread();

                window.completeInitForward(bottomSortKey);

                editList(Modifications.replace(items), true);

                if (items.size() == 0) {
                    window.onForwardCompleted();
                }
                for (Modification<T> m : pendingModifications) {
                    editList(m);
                }
                pendingModifications.clear();
            }
        }, currentGeneration));
    }

    // Load more

    @MainThread
    private void loadMoreForward() {
        im.actor.runtime.Runtime.checkMainThread();

        // Log.d(TAG, "Requesting loading more...");

        if (isLoadMoreForwardRequested) {
            // Log.d(TAG, "Already requested");
            return;
        }

        if (!window.startForwardLoading()) {
            // Log.d(TAG, "Unable to start forward loading");
            return;
        }

        isLoadMoreForwardRequested = true;
        final int gen = currentGeneration;
        Log.d(TAG, "Loading more items...");
        final long start = System.currentTimeMillis();
        ListEngineDisplayLoadCallback<T> callback = cover(new ListEngineDisplayLoadCallback<T>() {
            @Override
            public void onLoaded(List<T> items, long topSortKey, long bottomSortKey) {
                im.actor.runtime.Runtime.checkMainThread();

                Log.d(TAG, "Items loaded in " + (System.currentTimeMillis() - start) + " ms");

                window.completeForwardLoading();

                if (items.size() == 0) {
                    window.onForwardCompleted();
                    // Log.d(TAG, "isLoadMoreForwardRequested = false: sync");
                    isLoadMoreForwardRequested = false;
                } else {
                    window.onForwardSliceLoaded(bottomSortKey);
                    if (linearLayoutCallback != null) linearLayoutCallback.setStackFromEnd(false);
                    editList(Modifications.addLoadMore(items), new Runnable() {
                        @Override
                        public void run() {
                            if (gen == currentGeneration) {
                                // Log.d(TAG, "isLoadMoreForwardRequested = false");
                                isLoadMoreForwardRequested = false;
                            }
                        }
                    }, true);
                }
            }
        }, currentGeneration);

        if (mode != ListMode.SEARCH) {
            // Log.d(TAG, "Loading more...");
            listEngine.loadForward(window.getCurrentForwardHead(), pageSize, callback);
        } else {
            // Log.d(TAG, "Loading more search...");
            listEngine.loadForward(query, window.getCurrentForwardHead(), pageSize, callback);
        }
    }

    @MainThread
    private void loadMoreBackward() {
        im.actor.runtime.Runtime.checkMainThread();

        if (isLoadMoreBackwardRequested) {
            return;
        }

        if (!window.startBackwardLoading()) {
            return;
        }

        isLoadMoreBackwardRequested = true;
        final int gen = currentGeneration;
        ListEngineDisplayLoadCallback<T> callback = cover(new ListEngineDisplayLoadCallback<T>() {
            @Override
            public void onLoaded(List<T> items, long topSortKey, long bottomSortKey) {
                im.actor.runtime.Runtime.checkMainThread();

                window.endBackwardLoading();

                if (items.size() == 0) {
                    window.onBackwardCompleted();
                    // Generation already checked
                    isLoadMoreBackwardRequested = false;
                } else {
                    window.onBackwardSliceLoaded(topSortKey);
                    if (linearLayoutCallback != null) linearLayoutCallback.setStackFromEnd(true);
                    editList(Modifications.addLoadMore(items), new Runnable() {
                        @Override
                        public void run() {
                            if (gen == currentGeneration) {
                                isLoadMoreBackwardRequested = false;
                            }
                        }
                    }, true);
                }
            }
        }, currentGeneration);

        if (mode != ListMode.SEARCH) {
            listEngine.loadBackward(window.getCurrentBackwardHead(), pageSize, callback);
        } else {
            listEngine.loadBackward(query, window.getCurrentBackwardHead(), pageSize, callback);
        }
    }

    @MainThread
    @ObjectiveCName("dispose")
    public void dispose() {
        im.actor.runtime.Runtime.checkMainThread();

        if (isGlobalList) {
            throw new RuntimeException("Global DisplayList can't be disposed");
        }

        listEngine.unsubscribe(engineListener);
    }

    private ListEngineDisplayLoadCallback<T> cover(final ListEngineDisplayLoadCallback<T> callback, final int generation) {
        return new ListEngineDisplayLoadCallback<T>() {
            @Override
            public void onLoaded(final List<T> items, final long topSortKey, final long bottomSortKey) {
                im.actor.runtime.Runtime.postToMainThread(new Runnable() {
                    @Override
                    public void run() {
                        if (generation != currentGeneration) {
                            return;
                        }
                        callback.onLoaded(items, topSortKey, bottomSortKey);
                    }
                });
            }
        };
    }

    private class EngineListener implements ListEngineDisplayListener<T> {

        private void applyModification(final Modification<T> modification) {
            im.actor.runtime.Runtime.postToMainThread(new Runnable() {
                @Override
                public void run() {
                    if (window.isInited()) {
                        // TODO: Check if message from window
                        if (linearLayoutCallback != null)
                            linearLayoutCallback.setStackFromEnd(false);
                        editList(modification);
                    } else {
                        pendingModifications.add(modification);
                    }
                }
            });
        }

        @Override
        public void onItemRemoved(long id) {
            final Modification<T> modification = Modifications.remove(id);
            applyModification(modification);
        }

        @Override
        public void onItemsRemoved(long[] ids) {
            Modification<T> modification = Modifications.remove(ids);
            applyModification(modification);
        }

        @Override
        public void addOrUpdate(T item) {
            Modification<T> modification = Modifications.addOrUpdate(item);
            applyModification(modification);
        }

        @Override
        public void addOrUpdate(List<T> items) {
            Modification<T> modification = Modifications.addOrUpdate(items);
            applyModification(modification);
        }

        @Override
        public void onItemsReplaced(List<T> items) {
            Modification<T> modification = Modifications.replace(items);
            applyModification(modification);
        }

        @Override
        public void onListClear() {
            Modification<T> modification = Modifications.clear();
            applyModification(modification);
        }
    }

    private enum ListMode {
        FORWARD, BACKWARD, CENTER, SEARCH
    }

    public enum State {
        LOADING_EMPTY, LOADED, LOADED_EMPTY
    }

    public interface BindHook<T> {
        void onScrolledToEnd();

        void onItemTouched(T item);
    }

    public interface LinearLayoutCallback {
        void setStackFromEnd(boolean b);
    }

    public void setLinearLayoutCallback(LinearLayoutCallback linearLayoutCallback) {
        this.linearLayoutCallback = linearLayoutCallback;
    }
}