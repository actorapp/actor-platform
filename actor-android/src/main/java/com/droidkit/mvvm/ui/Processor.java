package com.droidkit.mvvm.ui;

/**
 * Created by ex3ndr on 18.09.14.
 */
public interface Processor<T, V> {
    public void process(T obj, V val);
}
