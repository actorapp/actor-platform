package com.droidkit.engine.list.view;

import com.droidkit.engine.list.DataAdapter;
import com.droidkit.engine.uilist.ListModification;

import java.util.*;

/**
 * Created by ex3ndr on 21.09.14.
 */
class ListMods<V> {
    private DataAdapter<V> dataAdapter;
    private Comparator<V> comparator;

    public ListMods(final DataAdapter<V> dataAdapter) {
        this.dataAdapter = dataAdapter;
        this.comparator = new Comparator<V>() {
            @Override
            public int compare(V lhs, V rhs) {
                long lKey = dataAdapter.getSortKey(lhs);
                long rKey = dataAdapter.getSortKey(rhs);

                if (lKey > rKey) {
                    return -1;
                } else if (lKey < rKey) {
                    return 1;
                } else {
                    return 0;
                }
            }
        };
    }

    public ListModification<V> add(final V item) {
        return new ListModification<V>() {
            @Override
            public void modify(List<V> arrayList, boolean isLast) {
                long dstId = dataAdapter.getId(item);
                Iterator<V> iterator = arrayList.iterator();
                while (iterator.hasNext()) {
                    V src = iterator.next();
                    long srcId = dataAdapter.getId(src);
                    if (srcId == dstId) {
                        iterator.remove();
                    }
                }
                arrayList.add(item);
                if (isLast) {
                    Collections.sort(arrayList, comparator);
                }
            }
        };
    }

    public ListModification<V> add(final List<V> items) {
        return new ListModification<V>() {
            @Override
            public void modify(List<V> arrayList, boolean isLast) {
                HashSet<Long> keys = new HashSet<Long>();
                for (V i : items) {
                    keys.add(dataAdapter.getId(i));
                }
                Iterator<V> iterator = arrayList.iterator();
                while (iterator.hasNext()) {
                    V src = iterator.next();
                    long srcId = dataAdapter.getId(src);
                    if (keys.contains(srcId)) {
                        iterator.remove();
                    }
                }
                arrayList.addAll(items);

                if (isLast) {
                    Collections.sort(arrayList, comparator);
                }
            }
        };
    }

    public ListModification<V> replace(final List<V> items) {
        return new ListModification<V>() {
            @Override
            public void modify(List<V> arrayList, boolean isLast) {
                arrayList.clear();
                arrayList.addAll(items);
                if (isLast) {
                    Collections.sort(arrayList, comparator);
                }
            }
        };
    }

    public ListModification<V> remove(final long id) {
        return new ListModification<V>() {
            @Override
            public void modify(List<V> arrayList, boolean isLast) {
                Iterator<V> it = arrayList.iterator();
                while (it.hasNext()) {
                    V value = it.next();
                    if (dataAdapter.getId(value) == id) {
                        it.remove();
                        break;
                    }
                }
                if (isLast) {
                    Collections.sort(arrayList, comparator);
                }
            }
        };
    }

    public ListModification<V> remove(final long[] ids) {
        return new ListModification<V>() {
            @Override
            public void modify(List<V> arrayList, boolean isLast) {
                Iterator<V> it = arrayList.iterator();
                while (it.hasNext()) {
                    V value = it.next();
                    long srcId = dataAdapter.getId(value);
                    for (long dstId : ids) {
                        if (dstId == srcId) {
                            it.remove();
                            break;
                        }
                    }
                }

                if (isLast) {
                    Collections.sort(arrayList, comparator);
                }
            }
        };
    }

    public ListModification<V> clear() {
        return new ListModification<V>() {
            @Override
            public void modify(List<V> arrayList, boolean isLast) {
                arrayList.clear();
            }
        };
    }
}
