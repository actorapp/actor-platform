package com.droidkit.engine.search._internal;

import com.droidkit.actors.ActorCreator;
import com.droidkit.actors.ActorSelection;
import com.droidkit.actors.Props;
import com.droidkit.actors.typed.TypedActor;
import com.droidkit.engine.common.ValuesCallback;
import com.droidkit.engine.search.DataAdapter;
import com.droidkit.engine.search.SearchAdapter;
import com.droidkit.engine.search.ValueContainer;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ex3ndr on 19.09.14.
 */
public class QueryActor<T> extends TypedActor<QueryInt> implements QueryInt<T> {

    public static ActorSelection queryActor(int id, final DataAdapter dataAdapter, final SearchAdapter searchAdapter) {
        return new ActorSelection(Props.create(QueryActor.class, new ActorCreator<QueryActor>() {
            @Override
            public QueryActor create() {
                return new QueryActor(dataAdapter, searchAdapter);
            }
        }).changeDispatcher("db"), "search_query_" + id);
    }

    private DataAdapter<T> dataAdapter;
    private SearchAdapter searchAdapter;

    private List<ValueContainer> prevQueryRes = null;
    private String prevQueryValue = null;

    public QueryActor(DataAdapter<T> dataAdapter, SearchAdapter searchAdapter) {
        super(QueryInt.class);
        this.dataAdapter = dataAdapter;
        this.searchAdapter = searchAdapter;
    }

    private String normalizeQuery(String q) {
        return q.trim().toLowerCase();
    }

    @Override
    public void index(long key, long order, String searchQuery, T data) {
        searchQuery = normalizeQuery(searchQuery);
        searchAdapter.index(new ValueContainer(key, order, searchQuery, dataAdapter.serialize(data)));
        // TODO: Optimize
        prevQueryValue = null;
        prevQueryRes = null;
    }

    @Override
    public void indexLow(long key, long order, String searchQuery, T data) {
        searchQuery = normalizeQuery(searchQuery);
        searchAdapter.indexLow(new ValueContainer(key, order, searchQuery, dataAdapter.serialize(data)));
        // TODO: Optimize
        prevQueryValue = null;
        prevQueryRes = null;
    }

    @Override
    public void clear() {
        searchAdapter.clear();
        // TODO: Optimize
        prevQueryValue = null;
        prevQueryRes = null;
    }

    @Override
    public void remove(long key) {
        searchAdapter.remove(key);
        // TODO: Optimize
        prevQueryValue = null;
        prevQueryRes = null;
    }

    @Override
    public void query(String query, ValuesCallback<T> callback) {
        query = normalizeQuery(query);
        if (prevQueryValue != null && query.startsWith(prevQueryValue)) {
            List<ValueContainer> filter = new ArrayList<ValueContainer>();
            for (ValueContainer c : prevQueryRes) {
                if (c.getQuery().startsWith(query) || c.getQuery().contains(" " + query)) {
                    filter.add(c);
                }
            }
            prevQueryValue = query;
            prevQueryRes = filter;
        } else {
            prevQueryValue = query;
            prevQueryRes = searchAdapter.query(query);
        }

        ArrayList<T> res = new ArrayList<T>();
        for (ValueContainer h : prevQueryRes) {
            res.add(dataAdapter.deserialize(h.getData()));
        }
        callback.values(res);
    }
}
