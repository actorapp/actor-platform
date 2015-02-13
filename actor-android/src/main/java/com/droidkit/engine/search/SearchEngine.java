package com.droidkit.engine.search;

import com.droidkit.actors.typed.TypedCreator;
import com.droidkit.engine.common.ValuesCallback;
import com.droidkit.engine.search._internal.QueryActor;
import com.droidkit.engine.search._internal.QueryInt;

import java.util.concurrent.atomic.AtomicInteger;

import static com.droidkit.actors.ActorSystem.system;

/**
 * Created by ex3ndr on 19.09.14.
 */
public class SearchEngine<T> {

    private static final AtomicInteger NEXT_ID = new AtomicInteger(1);

    private final int id;
    private final QueryInt<T> queryActor;

    public SearchEngine(DataAdapter<T> dataAdapter, SearchAdapter searchAdapter) {
        this.id = NEXT_ID.getAndIncrement();

        queryActor = TypedCreator.typed(system().actorOf(QueryActor.queryActor(id, dataAdapter, searchAdapter)),
                QueryInt.class);
    }

    public void index(long key, long order, String searchQuery, T data) {
        queryActor.index(key, order, searchQuery, data);
    }

    public void indexLow(long key, long order, String searchQuery, T data) {
        queryActor.indexLow(key, order, searchQuery, data);
    }

    public void remove(long key) {
        queryActor.remove(key);
    }

    public void query(String q, ValuesCallback<T> obj) {
        queryActor.query(q, obj);
    }

    public void clear() {
        queryActor.clear();
    }
}
