package com.droidkit.engine.search;

import java.util.List;

/**
 * Created by ex3ndr on 19.09.14.
 */
public interface SearchAdapter {

    public void clear();

    public void index(ValueContainer valueContainer);

    public void indexLow(ValueContainer valueContainer);

    public void remove(long key);

    public List<ValueContainer> query(String query);
}