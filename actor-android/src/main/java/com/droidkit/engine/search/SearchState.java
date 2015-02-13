package com.droidkit.engine.search;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ex3ndr on 23.09.14.
 */
public class SearchState<V> {
    private State state;
    private List<V> values;

    public SearchState(State state, List<V> values) {
        this.state = state;
        this.values = values;
    }

    public SearchState(State state) {
        this.state = state;
        this.values = new ArrayList<V>();
    }

    public List<V> getValues() {
        return values;
    }

    public State getState() {
        return state;
    }

    public enum State {
        EMPTY, SEARCHING, COMPLETED
    }
}
