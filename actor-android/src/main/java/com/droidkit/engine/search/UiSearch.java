package com.droidkit.engine.search;

import com.droidkit.engine.common.ValuesCallback;
import com.droidkit.engine.uilist.Modifications;
import com.droidkit.engine.uilist.UiList;
import com.droidkit.mvvm.ValueModel;

import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by ex3ndr on 19.09.14.
 */
public class UiSearch<T> {
    private final Object LOCK = new Object();
    private AtomicInteger REQ_ID = new AtomicInteger(1);

    private SearchEngine<T> engine;
    private UiList<T> resultList;

    private String currentQuery;
    private int currentRequest;

    private ValueModel<SearchState<T>> state;

    public UiSearch(SearchEngine<T> engine) {
        this.engine = engine;
        this.resultList = new UiList<T>();
        this.currentQuery = "";
        this.state = new ValueModel<SearchState<T>>("search.state", new SearchState<T>(SearchState.State.EMPTY));
    }

    public UiList<T> getResultList() {
        return resultList;
    }

    public void clear() {
        synchronized (LOCK) {
            resultList.editList(Modifications.clear());
            currentRequest = REQ_ID.incrementAndGet();
        }
    }

    public String getCurrentQuery() {
        return currentQuery;
    }

    public void query(String query) {
        query = query.trim();
        synchronized (LOCK) {
            currentQuery = query;
            currentRequest = REQ_ID.incrementAndGet();
            this.state.change(new SearchState<T>(SearchState.State.SEARCHING));
            if (query.length() > 0) {
                final int req = currentRequest;
                engine.query(query, new ValuesCallback<T>() {
                    @Override
                    public void values(ArrayList<T> value) {
                        synchronized (LOCK) {
                            if (currentRequest != req) {
                                return;
                            }
                            resultList.editList(Modifications.replace(value));
                            state.change(new SearchState<T>(SearchState.State.COMPLETED, value));
                        }
                    }
                });
            } else {
                resultList.editList(Modifications.clear());
                this.state.change(new SearchState<T>(SearchState.State.EMPTY));
            }
        }
    }
}
