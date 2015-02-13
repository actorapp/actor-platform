package com.droidkit.engine.persistence;

import java.util.*;

/**
 * Created by ex3ndr on 07.10.14.
 */
public class PersistenceSet<V> implements Set<V> {

    private HashSet<V> backedSet;
    private PersistenceMap<V> persistenceMap;
    private HashMap<V, Long> mapping;
    private long nextId;

    public PersistenceSet(PersistenceMap<V> persistenceMap) {
        this.persistenceMap = persistenceMap;
        this.backedSet = new HashSet<V>();
        this.mapping = new HashMap<V, Long>();

        for (Long key : persistenceMap.keySet()) {
            V val = persistenceMap.get(key);
            backedSet.add(val);
            mapping.put(val, key);
            if (key > nextId) {
                nextId = key + 1;
            }
        }
    }

    private void addItem(V object) {
        Long id = nextId++;
        persistenceMap.put(id, object);
        mapping.put(object, id);
    }

    private void removeItem(Object object) {
        Long id = mapping.remove(object);
        if (id != null) {
            persistenceMap.remove(id);
        }
    }

    @Override
    public boolean add(V object) {
        boolean res = backedSet.add(object);
        if (res) {
            removeItem(object);
        }
        addItem(object);
        return res;
    }

    @Override
    public boolean addAll(Collection<? extends V> collection) {
        boolean res = false;
        Iterator<? extends V> it = collection.iterator();
        while (it.hasNext()) {
            if (add(it.next())) {
                res = true;
            }
        }
        return res;
    }

    @Override
    public void clear() {
        persistenceMap.clear();
        backedSet.clear();
    }

    @Override
    public boolean contains(Object object) {
        return backedSet.contains(object);
    }

    @Override
    public boolean containsAll(Collection<?> collection) {
        return backedSet.containsAll(collection);
    }

    @Override
    public boolean isEmpty() {
        return backedSet.isEmpty();
    }

    @Override
    public Iterator<V> iterator() {
        return backedSet.iterator();
    }

    @Override
    public boolean remove(Object object) {
        if (backedSet.remove(object)) {
            removeItem(object);
            return true;
        }
        return false;
    }

    @Override
    public boolean removeAll(Collection<?> collection) {
        boolean res = false;
        Iterator<?> it = collection.iterator();
        while (it.hasNext()) {
            if (remove(it.next())) {
                res = true;
            }
        }
        return res;
    }

    @Override
    public boolean retainAll(Collection<?> collection) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int size() {
        return backedSet.size();
    }

    @Override
    public Object[] toArray() {
        return backedSet.toArray();
    }

    @Override
    public <T> T[] toArray(T[] array) {
        return backedSet.toArray(array);
    }
}
