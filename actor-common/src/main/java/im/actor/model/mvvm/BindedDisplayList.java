package im.actor.model.mvvm;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import im.actor.model.annotation.MainThread;
import im.actor.model.droidkit.bser.BserObject;
import im.actor.model.droidkit.engine.ListEngineCallback;
import im.actor.model.droidkit.engine.ListEngineDisplayExt;
import im.actor.model.droidkit.engine.ListEngineDisplayListener;
import im.actor.model.droidkit.engine.ListEngineItem;

/**
 * Created by ex3ndr on 14.03.15.
 */
public class BindedDisplayList<T extends BserObject & ListEngineItem> extends DisplayList<T> {

    private static final int DEFAULT_PAGE_SIZE = 5;

    private static final Comparator<ListEngineItem> COMPARATOR = new ListEngineComparator();

    private int pageSize = DEFAULT_PAGE_SIZE;

    private final ListEngineDisplayExt<T> listEngine;
    private final DisplayWindow window;
    private final EngineListener engineListener;

    private int currentGeneration = 0;

    private final boolean isGlobalList;

    private ValueModel<State> stateModel;

    private ListMode mode;
    private String query;

    public BindedDisplayList(ListEngineDisplayExt<T> listEngine, boolean isGlobalList) {
        super(new Hook<T>() {
            @Override
            public void beforeDisplay(List<T> list) {
                Collections.sort(list, COMPARATOR);
            }
        });
        this.engineListener = new EngineListener();
        this.isGlobalList = isGlobalList;
        this.listEngine = listEngine;
        this.window = new DisplayWindow();
        this.stateModel = new ValueModel<State>("display_list.state", State.LOADING_EMPTY);

        listEngine.subscribe(engineListener);
    }

    @Deprecated
    public int getPageSize() {
        return pageSize;
    }

    @Deprecated
    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }

    // Init methods

    @MainThread
    public void initTop(boolean refresh) {
        MVVMEngine.checkMainThread();

        if (mode != null && mode == ListMode.FORWARD) {
            return;
        }
        mode = ListMode.FORWARD;
        query = null;

        if (refresh) {
            editList((Modification) DisplayModifications.clear());
        }

        stateModel.change(State.LOADING_EMPTY);
        currentGeneration++;
        window.startInitForward();
        listEngine.loadForward(pageSize, cover(new ListEngineCallback<T>() {
            @Override
            public void onLoaded(List<T> items, long topSortKey, long bottomSortKey) {
                MVVMEngine.checkMainThread();

                window.completeInitForward(bottomSortKey);

                if (items.size() != 0) {
                    editList(DisplayModifications.replace(items));
                } else {
                    window.onForwardCompleted();
                }
            }
        }, currentGeneration));
    }

    @MainThread
    public void initBottom(boolean refresh) {
        MVVMEngine.checkMainThread();

        if (isGlobalList) {
            throw new RuntimeException("Global DisplayList can't grow from bottom");
        }

        if (mode != null && mode == ListMode.BACKWARD) {
            return;
        }
        mode = ListMode.BACKWARD;

        if (refresh) {
            editList((Modification) DisplayModifications.clear());
        }

        stateModel.change(State.LOADING_EMPTY);
        currentGeneration++;
        window.startInitBackward();

        listEngine.loadBackward(pageSize, cover(new ListEngineCallback<T>() {
            @Override
            public void onLoaded(List<T> items, long topSortKey, long bottomSortKey) {
                MVVMEngine.checkMainThread();

                window.completeInitBackward(topSortKey);

                if (items.size() != 0) {
                    editList(DisplayModifications.replace(items));
                } else {
                    window.onBackwardCompleted();
                }
            }
        }, currentGeneration));
    }

    @MainThread
    public void initCenter(long centerSortKey, boolean refresh) {
        MVVMEngine.checkMainThread();

        if (mode != null && mode == ListMode.CENTER) {
            return;
        }
        mode = ListMode.CENTER;

        if (refresh) {
            editList((Modification) DisplayModifications.clear());
        }

        stateModel.change(State.LOADING_EMPTY);
        currentGeneration++;
        window.startInitCenter();

        listEngine.loadCenter(centerSortKey, pageSize, cover(new ListEngineCallback<T>() {
            @Override
            public void onLoaded(List<T> items, long topSortKey, long bottomSortKey) {
                MVVMEngine.checkMainThread();

                window.completeInitCenter(bottomSortKey, topSortKey);

                if (items.size() != 0) {
                    editList(DisplayModifications.addOrUpdate(items));
                } else {
                    window.onForwardCompleted();
                    window.onBackwardCompleted();
                }
            }
        }, currentGeneration));
    }

    @MainThread
    public void initSearch(String query, boolean refresh) {
        MVVMEngine.checkMainThread();

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
            editList((Modification) DisplayModifications.clear());
        }

        stateModel.change(State.LOADING_EMPTY);
        currentGeneration++;
        window.startInitForward();

        listEngine.loadForward(query, pageSize, cover(new ListEngineCallback<T>() {
            @Override
            public void onLoaded(List<T> items, long topSortKey, long bottomSortKey) {
                MVVMEngine.checkMainThread();

                window.completeInitForward(bottomSortKey);

                editList(DisplayModifications.replace(items));

                if (items.size() == 0) {
                    window.onForwardCompleted();
                }
            }
        }, currentGeneration));
    }

    // Load more

    @MainThread
    public void loadMoreForward() {
        MVVMEngine.checkMainThread();

        if (!window.startForwardLoading()) {
            return;
        }

        ListEngineCallback<T> callback = cover(new ListEngineCallback<T>() {
            @Override
            public void onLoaded(List<T> items, long topSortKey, long bottomSortKey) {
                MVVMEngine.checkMainThread();

                window.completeForwardLoading();

                if (items.size() == 0) {
                    window.onForwardCompleted();
                } else {
                    window.onForwardSliceLoaded(bottomSortKey);
                }

                editList(DisplayModifications.addOrUpdate(items));
            }
        }, currentGeneration);

        if (mode != ListMode.SEARCH) {
            listEngine.loadForward(window.getCurrentForwardHead(), pageSize, callback);
        } else {
            listEngine.loadForward(query, window.getCurrentForwardHead(), pageSize, callback);
        }
    }

    @MainThread
    public void loadMoreBackward() {
        MVVMEngine.checkMainThread();

        if (!window.startHeadLoading()) {
            return;
        }

        ListEngineCallback<T> callback = cover(new ListEngineCallback<T>() {
            @Override
            public void onLoaded(List<T> items, long topSortKey, long bottomSortKey) {
                MVVMEngine.checkMainThread();

                if (items.size() == 0) {
                    window.onBackwardCompleted();
                } else {
                    window.onBackwardSliceLoaded(bottomSortKey);
                }
                window.endBackwardLoading();
            }
        }, currentGeneration);

        if (mode != ListMode.SEARCH) {
            listEngine.loadBackward(window.getCurrentBackwardHead(), pageSize, callback);
        } else {
            listEngine.loadBackward(query, window.getCurrentBackwardHead(), pageSize, callback);
        }
    }

    @MainThread
    public void dispose() {
        MVVMEngine.checkMainThread();

        if (isGlobalList) {
            throw new RuntimeException("Global DisplayList can't be disposed");
        }

        listEngine.unsubscribe(engineListener);
    }

    private ListEngineCallback<T> cover(final ListEngineCallback<T> callback, final int generation) {
        return new ListEngineCallback<T>() {
            @Override
            public void onLoaded(final List<T> items, final long topSortKey, final long bottomSortKey) {
                MVVMEngine.runOnUiThread(new Runnable() {
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

    private static class ListEngineComparator implements Comparator<ListEngineItem> {

        @Override
        public int compare(ListEngineItem o1, ListEngineItem o2) {
            long lKey = o1.getEngineSort();
            long rKey = o2.getEngineSort();

            if (lKey > rKey) {
                return -1;
            } else if (lKey < rKey) {
                return 1;
            } else {
                return 0;
            }
        }
    }

    private class EngineListener implements ListEngineDisplayListener<T> {

        @Override
        public void onItemRemoved(long id) {
            // TODO: Check if message from window
            editList((Modification) DisplayModifications.remove(id));
        }

        @Override
        public void onItemsRemoved(long[] ids) {
            // TODO: Check if message from window
            editList((Modification) DisplayModifications.remove(ids));
        }

        @Override
        public void addOrUpdate(T item) {
            // TODO: Check if message from window
            editList(DisplayModifications.addOrUpdate(item));
        }

        @Override
        public void addOrUpdate(List<T> items) {
            // TODO: Check if message from window
            editList(DisplayModifications.addOrUpdate(items));
        }

        @Override
        public void onItemsReplaced(List<T> items) {
            // TODO: Check if message from window
            editList((Modification) DisplayModifications.clear());
            editList(DisplayModifications.addOrUpdate(items));
        }

        @Override
        public void onListClear() {
            // TODO: Check if message from window
            editList((Modification) DisplayModifications.clear());
        }
    }

    private enum ListMode {
        FORWARD, BACKWARD, CENTER, SEARCH
    }

    public enum State {
        LOADING_EMPTY, LOADED, LOADED_EMPTY
    }
}