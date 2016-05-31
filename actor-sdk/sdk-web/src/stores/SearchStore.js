/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

import { ReduceStore } from 'flux/utils';
import Dispatcher from '../dispatcher/ActorAppDispatcher';
import { ActionTypes } from '../constants/ActorAppConstants';

class SearchStore extends ReduceStore {
  getInitialState() {
    return {
      query: '',
      results: [],
      isFocused: false
    };
  }

  reduce(state, action) {
    switch (action.type) {
      case ActionTypes.SEARCH_CLEAR:
        return this.getInitialState();

      case ActionTypes.SEARCH_FOCUS:
        return {
          ...state,
          isFocused: true
        };

      case ActionTypes.SEARCH_BLUR:
        return {
          ...state,
          isFocused: false
        };
      case ActionTypes.SEARCH_SET_QUERY:
        return {
          ...state,
          query: action.query
        };

      case ActionTypes.SEARCH_SET_RESULTS:
        return {
          ...state,
          results: action.results
        };

      default:
        return state;
    }
  }
}

export default new SearchStore(Dispatcher);
