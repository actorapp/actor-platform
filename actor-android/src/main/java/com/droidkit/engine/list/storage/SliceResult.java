package com.droidkit.engine.list.storage;

import java.util.List;

/**
 * Created by ex3ndr on 10.09.14.
 */
public class SliceResult<V> {
    private List<V> values;
    private Object bottomKey;
    private Object topKey;

    public SliceResult(List<V> values, Object bottomKey, Object topKey) {
        this.values = values;
        this.bottomKey = bottomKey;
        this.topKey = topKey;
    }

    public List<V> getValues() {
        return values;
    }

    public Object getBottomKey() {
        return bottomKey;
    }

    public Object getTopKey() {
        return topKey;
    }
}
