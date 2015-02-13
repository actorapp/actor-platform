package com.droidkit.engine.list.actors;

import com.droidkit.actors.*;
import com.droidkit.actors.typed.TypedActor;
import com.droidkit.engine.list.DataAdapter;
import com.droidkit.engine.list.FilterableDataAdapter;
import com.droidkit.engine.list.LoadCallback;
import com.droidkit.engine.list.LoadCenterCallback;
import com.droidkit.engine.list.LoadItemCallback;
import com.droidkit.engine.list.storage.SliceResult;
import com.droidkit.engine.list.storage.StorageAdapter;
import com.droidkit.engine.list.storage.ValueContainer;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ex3ndr on 01.09.14.
 */
public class StorageActor<V> extends TypedActor<StorageActorInt> implements StorageActorInt<V> {
    public static <V> ActorSelection storage(int listEngineId, final StorageAdapter adapter, final DataAdapter<V> dataAdapter) {
        return new ActorSelection(Props.create(StorageActor.class, new ActorCreator<StorageActor>() {
            @Override
            public StorageActor create() {
                return new StorageActor(adapter, dataAdapter);
            }
        }).changeDispatcher("db"), "storage_" + listEngineId);
    }

    private final StorageAdapter adapter;
    private final DataAdapter<V> dataAdapter;

    public StorageActor(StorageAdapter adapter, DataAdapter<V> dataAdapter) {
        super(StorageActorInt.class);
        this.dataAdapter = dataAdapter;
        this.adapter = adapter;
    }

    private ValueContainer buildContainer(V value) {
        String filter = null;
        if (dataAdapter instanceof FilterableDataAdapter) {
            filter = ((FilterableDataAdapter) dataAdapter).getFilterValue(value);
        }
        return new ValueContainer(dataAdapter.getId(value),
                dataAdapter.getSortKey(value), filter, dataAdapter.serialize(value));
    }

    private V unpack(ValueContainer container) {
        if (container == null) {
            return null;
        }
        return dataAdapter.deserialize(container.getData());
    }

    private List<V> unpack(List<ValueContainer> containers) {
        ArrayList<V> res = new ArrayList<V>();
        for (ValueContainer container : containers) {
            res.add(dataAdapter.deserialize(container.getData()));
        }
        return res;
    }

    @Override
    public void updateOrAdd(V value) {
        adapter.updateOrAdd(buildContainer(value));
    }

    @Override
    public void updateOrAdd(List<V> values) {
        if (values.size() == 0) {
            return;
        }
        if (values.size() == 1) {
            adapter.updateOrAdd(buildContainer(values.get(0)));
        } else {
            List<ValueContainer> containers = new ArrayList<ValueContainer>();
            for (V v : values) {
                containers.add(buildContainer(v));
            }
            adapter.updateOrAdd(containers);
        }
    }

    @Override
    public void delete(long key) {
        adapter.delete(key);
    }

    @Override
    public void delete(long[] keys) {
        adapter.delete(keys);
    }

    @Override
    public void clear() {
        adapter.clear();
    }

    @Override
    public void loadCenterInitial(long centerKey, int limit, LoadCenterCallback<V> callback) {
        SliceResult<ValueContainer> before = adapter.loadBefore(centerKey, limit);
        SliceResult<ValueContainer> after = adapter.loadAfter(centerKey, limit);

        SliceResult<ValueContainer> res;
        if (before.getValues().size() == 0 && after.getValues().size() == 0) {
            res = new SliceResult<ValueContainer>(new ArrayList<ValueContainer>(), null, null);
        } else if (before.getValues().size() == 0 && after.getValues().size() != 0) {
            res = after;
        } else if (after.getValues().size() == 0 && before.getValues().size() != 0) {
            res = before;
        } else {
            ArrayList<ValueContainer> items = new ArrayList<ValueContainer>();
            items.addAll(before.getValues());
            items.addAll(after.getValues());
            res = new SliceResult<ValueContainer>(items, before.getBottomKey(), after.getBottomKey());
        }

        callback.onLoaded(unpack(res.getValues()), res.getBottomKey(), res.getTopKey());
    }

    @Override
    public void loadTailInitial(int limit, LoadCallback<V> callback) {
        SliceResult<ValueContainer> sliceResult = adapter.loadTail(null, limit);
        callback.onLoaded(unpack(sliceResult.getValues()), sliceResult.getBottomKey());
    }

    @Override
    public void loadTail(Object key, int limit, LoadCallback<V> callback) {
        SliceResult<ValueContainer> sliceResult = adapter.loadTail(key, limit);
        callback.onLoaded(unpack(sliceResult.getValues()), sliceResult.getBottomKey());
    }

    @Override
    public void loadHeadInitial(int limit, LoadCallback<V> callback) {
        SliceResult<ValueContainer> sliceResult = adapter.loadHead(null, limit);
        callback.onLoaded(unpack(sliceResult.getValues()), sliceResult.getBottomKey());
    }

    @Override
    public void loadHead(Object key, int limit, LoadCallback<V> callback) {
        SliceResult<ValueContainer> sliceResult = adapter.loadHead(key, limit);
        callback.onLoaded(unpack(sliceResult.getValues()), sliceResult.getBottomKey());
    }

    @Override
    public void loadTailInitial(String query, int limit, LoadCallback<V> callback) {
        SliceResult<ValueContainer> sliceResult = adapter.loadTail(query, null, limit);
        callback.onLoaded(unpack(sliceResult.getValues()), sliceResult.getBottomKey());
    }

    @Override
    public void loadTail(String query, Object key, int limit, LoadCallback<V> callback) {
        SliceResult<ValueContainer> sliceResult = adapter.loadTail(query, key, limit);
        callback.onLoaded(unpack(sliceResult.getValues()), sliceResult.getBottomKey());
    }

    @Override
    public void loadHeadInitial(String query, int limit, LoadCallback<V> callback) {
        SliceResult<ValueContainer> sliceResult = adapter.loadHead(query, null, limit);
        callback.onLoaded(unpack(sliceResult.getValues()), sliceResult.getBottomKey());
    }

    @Override
    public void loadHead(String query, Object key, int limit, LoadCallback<V> callback) {
        SliceResult<ValueContainer> sliceResult = adapter.loadHead(query, key, limit);
        callback.onLoaded(unpack(sliceResult.getValues()), sliceResult.getBottomKey());
    }

    @Override
    public void loadItem(long id, LoadItemCallback<V> callback) {
        V res = unpack(adapter.loadItem(id));
        callback.onLoaded(res);
    }

    @Override
    public void loadHeadValue(LoadItemCallback<V> callback) {
        SliceResult<ValueContainer> sliceResult = adapter.loadTail(null, 1);
        if (sliceResult.getValues().size() == 1) {
            callback.onLoaded(unpack(sliceResult.getValues().get(0)));
        } else {
            callback.onLoaded(null);
        }
    }

    @Override
    public void loadCount(LoadItemCallback<Integer> callback) {
        callback.onLoaded(adapter.getCount());
    }
}
