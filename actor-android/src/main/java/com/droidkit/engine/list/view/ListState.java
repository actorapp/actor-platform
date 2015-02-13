package com.droidkit.engine.list.view;

/**
 * Created by ex3ndr on 23.09.14.
 */
public class ListState {

    private State state;

    public ListState(State state) {
        this.state = state;
    }

    public State getState() {
        return state;
    }

    public enum State {
        LOADING_EMPTY, LOADED, LOADED_EMPTY
    }
}
