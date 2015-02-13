package com.droidkit.engine.uilist;

import java.util.List;

/**
 * Created by ex3ndr on 19.09.14.
 */
public class Modifications {

    public static <V> ListModification<V> diff(final List<V> toAdd, final List<V> toRemove) {
        return new ListModification<V>() {
            @Override
            public void modify(List<V> arrayList, boolean isLast) {
                arrayList.removeAll(toRemove);
                arrayList.addAll(toAdd);
            }
        };
    }

    public static <V> ListModification<V> remove(final List<V> toRemove) {
        return new ListModification<V>() {
            @Override
            public void modify(List<V> arrayList, boolean isLast) {
                arrayList.removeAll(toRemove);
            }
        };
    }

    public static <V> ListModification<V> remove(final V toRemove) {
        return new ListModification<V>() {
            @Override
            public void modify(List<V> arrayList, boolean isLast) {
                arrayList.remove(toRemove);
            }
        };
    }

    public static <V> ListModification<V> addToEnd(final List<V> toAdd) {
        return new ListModification<V>() {
            @Override
            public void modify(List<V> arrayList, boolean isLast) {
                arrayList.addAll(toAdd);
            }
        };
    }

    public static <V> ListModification<V> replace(final List<V> toAdd) {
        return new ListModification<V>() {
            @Override
            public void modify(List<V> arrayList, boolean isLast) {
                arrayList.clear();
                arrayList.addAll(toAdd);
            }
        };
    }

    public static <V> ListModification<V> add(final V toAdd) {
        return new ListModification<V>() {
            @Override
            public void modify(List<V> arrayList, boolean isLast) {
                arrayList.add(toAdd);
            }
        };
    }

    public static ListModification clear() {
        return new ListModification() {
            @Override
            public void modify(List arrayList, boolean isLast) {
                arrayList.clear();
            }
        };
    }
}
