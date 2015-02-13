package com.droidkit.engine.uilist;

import java.util.List;

/**
 * Created by ex3ndr on 19.09.14.
 */
public interface ListModification<V> {
    public void modify(List<V> arrayList, boolean isLast);
}
