package com.droidkit.engine.list;

import java.util.List;

/**
 * Created by ex3ndr on 11.09.14.
 */
public interface LoadCenterCallback<V> {
    public void onLoaded(List<V> res, Object prevKey, Object nextKey);
}
