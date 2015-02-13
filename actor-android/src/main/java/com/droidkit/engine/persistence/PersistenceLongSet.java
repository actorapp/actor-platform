package com.droidkit.engine.persistence;

import com.droidkit.engine.persistence.storage.PersistenceStorage;

import java.util.Collection;
import java.util.Iterator;
import java.util.Set;

/**
 * Created by ex3ndr on 30.09.14.
 */
public class PersistenceLongSet implements Set<Long> {

    private PersistenceMap<Boolean> map;

    public PersistenceLongSet(PersistenceStorage storage) {
        map = new SerializableMap<Boolean>(storage);
    }

    @Override
    public boolean add(Long object) {
        map.put(object, Boolean.TRUE);
        return false;
    }

    @Override
    public boolean addAll(Collection<? extends Long> collection) {
        for (Long l : collection) {
            map.put(l, Boolean.TRUE);
        }
        return false;
    }

    @Override
    public void clear() {
        map.clear();
    }

    @Override
    public boolean contains(Object object) {
        return map.containsKey(object);
    }

    @Override
    public boolean containsAll(Collection<?> collection) {
        for (Object o : collection) {
            if (!map.containsKey(o)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean isEmpty() {
        return map.isEmpty();
    }

    @Override
    public Iterator<Long> iterator() {
        return map.keySet().iterator();
    }

    @Override
    public boolean remove(Object object) {
        return map.remove(object);
    }

    public boolean removeAll(Long[] ids) {
        for (Long o : ids) {
            map.remove(o);
        }
        return true;
    }

    @Override
    public boolean removeAll(Collection<?> collection) {
        for (Object o : collection) {
            map.remove(o);
        }
        return true;
    }

    @Override
    public boolean retainAll(Collection<?> collection) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int size() {
        return map.size();
    }

    @Override
    public Object[] toArray() {
        return map.keySet().toArray();
    }

    @Override
    public <T> T[] toArray(T[] array) {
        return map.keySet().toArray(array);
    }
}
