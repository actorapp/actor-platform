package com.droidkit.engine.list.view;

import android.util.Log;

import com.droidkit.engine.list.DataAdapter;
import com.droidkit.engine.list.ListEngine;
import com.droidkit.engine.list.ListEngineCallback;
import com.droidkit.engine.list.LoadCallback;
import com.droidkit.engine.list.LoadCenterCallback;
import com.droidkit.engine.uilist.ListModification;
import com.droidkit.engine.uilist.UiList;
import com.droidkit.engine.uilist.UiListListener;
import com.droidkit.images.util.UiUtil;
import com.droidkit.mvvm.ValueModel;

import im.actor.messenger.util.Logger;

import java.util.List;

/**
 * Created by ex3ndr on 20.09.14.
 */
public class EngineUiList<V> {
    private UiList<V> uiList;

    private final ListWindow listWindow = new ListWindow();

    private int loadLimit;

    private ListEngine<V> listEngine;

    private ListCallback callback;

    private DataAdapter<V> dataAdapter;

    private ListMods<V> listMods;

    private ValueModel<ListState> listState;

    private String query;

    public EngineUiList(ListEngine<V> listEngine) {
        this(listEngine, 20, true);
    }

    public EngineUiList(ListEngine<V> listEngine, int loadLimit) {
        this(listEngine, loadLimit, true);
    }

    public EngineUiList(ListEngine<V> listEngine, int loadLimit, boolean autoLoad) {
        this.loadLimit = loadLimit;
        this.callback = new ListCallback();
        this.listEngine = listEngine;
        this.listEngine.addListener(callback);
        this.dataAdapter = listEngine.getDataAdapter();
        this.uiList = new UiList<V>();
        this.uiList.addListener(new UiListListener() {
            @Override
            public void onListUpdated() {
                if (uiList.getSize() == 0) {
                    if (listWindow.isInited() && listWindow.isTailLoaded() && listWindow.isHeadLoaded()) {
                        listState.change(new ListState(ListState.State.LOADED_EMPTY));
                    } else {
                        listState.change(new ListState(ListState.State.LOADING_EMPTY));
                    }
                } else {
                    listState.change(new ListState(ListState.State.LOADED));
                }
            }
        });
        this.listMods = new ListMods<V>(dataAdapter);
        this.listState = new ValueModel<ListState>("list_engine.state", new ListState(ListState.State.LOADING_EMPTY));
        if (autoLoad) {
            initGeneral();
        }
    }

    public ValueModel<ListState> getListState() {
        return listState;
    }

    public UiList<V> getUiList() {
        return uiList;
    }

    private void initForward(boolean fast) {
        this.query = null;
        if (fast) {
            this.listState.change(new ListState(ListState.State.LOADING_EMPTY));
            listWindow.startInitForward();
            final long start = System.currentTimeMillis();
            final Object[] result = new Object[2];
            listEngine.loadTailInitial(loadLimit, new LoadCallback<V>() {
                @Override
                public void onLoaded(List<V> res, Object nextRef) {
                    synchronized (result) {
                        result[0] = res;
                        result[1] = nextRef;
                        result.notify();
                    }

                }
            });

            synchronized (result) {
                if (result[0] == null) {
                    try {
                        result.wait();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                        return;
                    }
                }
            }

            List<V> res = (List<V>) result[0];
            Object nextRef = result[1];
            uiList.setList(listMods.replace(res));
            // Logger.d("EngineUiList", "Init in " + (System.currentTimeMillis() - start) + " ms");
            listWindow.stopInitForward(nextRef);
            if (res.size() == 0) {
                listState.change(new ListState(ListState.State.LOADED_EMPTY));
                listWindow.onTailCompleted();
            } else {
                listState.change(new ListState(ListState.State.LOADED));
            }
        } else {
            this.listState.change(new ListState(ListState.State.LOADING_EMPTY));
            listWindow.startInitForward();
            final long start = System.currentTimeMillis();
            listEngine.loadTailInitial(loadLimit, new LoadCallback<V>() {
                @Override
                public void onLoaded(List<V> res, Object nextRef) {
                    uiList.editList(listMods.replace(res));
                    // Logger.d("EngineUiList", "Init in " + (System.currentTimeMillis() - start) + " ms");
                    listWindow.stopInitForward(nextRef);
                    if (res.size() == 0) {
                        listState.change(new ListState(ListState.State.LOADED_EMPTY));
                        listWindow.onTailCompleted();
                    } else {
                        listState.change(new ListState(ListState.State.LOADED));
                    }
                }
            });
        }
    }

    private void initCenter(long key) {
        this.listState.change(new ListState(ListState.State.LOADING_EMPTY));
        listWindow.startInitCenter();
        listEngine.loadCenterInitial(key, loadLimit, new LoadCenterCallback<V>() {
            @Override
            public void onLoaded(List<V> res, Object prevKey, Object nextKey) {
                uiList.editList(listMods.replace(res));
                listWindow.stopInitCenter(prevKey, nextKey);
                if (res.size() == 0) {
                    listState.change(new ListState(ListState.State.LOADED_EMPTY));
                    listWindow.onTailCompleted();
                    listWindow.onHeadCompleted();
                } else {
                    listState.change(new ListState(ListState.State.LOADED));
                }
            }
        });
    }

    private void initForwardSearch(String query) {
        this.query = query;
        this.listState.change(new ListState(ListState.State.LOADING_EMPTY));
        listWindow.startInitForward();
        listEngine.loadTailInitial(query, loadLimit, new LoadCallback<V>() {
            @Override
            public void onLoaded(List<V> res, Object nextRef) {
                uiList.editList(listMods.replace(res));
                listWindow.stopInitForward(nextRef);
                if (res.size() == 0) {
                    listState.change(new ListState(ListState.State.LOADED_EMPTY));
                    listWindow.onTailCompleted();
                } else {
                    listState.change(new ListState(ListState.State.LOADED));
                }
            }
        });
    }

    private void initBackward() {
        this.query = null;
        this.listState.change(new ListState(ListState.State.LOADING_EMPTY));
        listWindow.startInitBackward();
        listEngine.loadHeadInitial(loadLimit, new LoadCallback<V>() {
            @Override
            public void onLoaded(List<V> res, Object nextRef) {
                listWindow.stopInitBackward(nextRef);
                uiList.editList(listMods.replace(res));
                if (res.size() == 0) {
                    listWindow.onHeadCompleted();
                }
            }
        });
    }

    private void clearList() {
        uiList.editList(listMods.clear());
    }

    public void scrollToItem(long sortingKey) {
        clearList();
        initCenter(sortingKey);
    }

    public void scrollToEnd() {
        scrollToEnd(true);
    }

    public void scrollToEnd(boolean clear) {
        if (clear) {
            clearList();
        }
        initBackward();
    }

    public void scrollToStart() {
        scrollToStart(true);
    }

    public void initGeneral() {
        initForward(UiUtil.isMainThread());
    }

    public void scrollToStart(boolean clear) {
        if (clear) {
            clearList();
        }
        initForward(true);
    }

    public void filter(String query) {
        filter(query, false);
    }

    public void filter(String query, boolean clear) {
        query = query.trim();

        if (query.length() == 0) {
            if (this.query != null) {
                if (clear) {
                    clearList();
                }
                initForward(false);
            }
        } else {
            if (clear) {
                clearList();
            }
            initForwardSearch(query);
        }
    }

    // Updating list
    public void requestLoadTail() {
        if (!listWindow.startTailLoading()) {
            return;
        }

        if (query != null) {
            listEngine.loadTail(query, listWindow.getCurrentTail(), loadLimit, new LoadCallback<V>() {
                @Override
                public void onLoaded(List<V> res, Object nextRef) {
                    if (res.size() == 0) {
                        listWindow.onTailCompleted();
                    } else {
                        listWindow.onTailSliceLoaded(nextRef);
                    }
                    listWindow.endTileLoading();
                    uiList.editList(listMods.add(res));
                }
            });
        } else {
            listEngine.loadTail(listWindow.getCurrentTail(), loadLimit, new LoadCallback<V>() {
                @Override
                public void onLoaded(List<V> res, Object nextRef) {
                    if (res.size() == 0) {
                        listWindow.onTailCompleted();
                    } else {
                        listWindow.onTailSliceLoaded(nextRef);
                    }
                    listWindow.endTileLoading();
                    uiList.editList(listMods.add(res));
                }
            });
        }
    }

    public void requestLoadHead() {
        if (!listWindow.startHeadLoading()) {
            return;
        }

        listEngine.loadHead(listWindow.getCurrentHead(), loadLimit, new LoadCallback<V>() {
            @Override
            public void onLoaded(List<V> res, Object nextRef) {
                if (res.size() == 0) {
                    listWindow.onHeadCompleted();
                } else {
                    listWindow.onHeadSliceLoaded(nextRef);
                }
                listWindow.endHeadLoading();
                uiList.editList(listMods.add(res));
            }
        });
    }

    public void release() {
        listEngine.removeListener(callback);
    }

    private class ListCallback implements ListEngineCallback<V> {

        @Override
        public void onItemRemoved(long id) {
            uiList.editList(listMods.remove(id));
        }

        @Override
        public void onItemsRemoved(long[] id) {
            uiList.editList(listMods.remove(id));
        }

        @Override
        public void addOrUpdate(V item) {
            uiList.editList(listMods.add(item));
        }

        @Override
        public void addOrUpdate(List<V> items) {
            uiList.editList(listMods.add(items));
        }

        @Override
        public void onItemsReplaced(List<V> items) {
            uiList.editList(listMods.replace(items));
        }

        @Override
        public void onListClear() {
            uiList.editList(listMods.clear());
        }
    }
}
