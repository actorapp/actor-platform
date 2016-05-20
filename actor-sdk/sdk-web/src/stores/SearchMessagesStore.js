/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

import { ReduceStore } from 'flux/utils';
import Dispatcher from '../dispatcher/ActorAppDispatcher';
import { ActionTypes } from '../constants/ActorAppConstants';

class SearchMessagesStore extends ReduceStore {
  getInitialState() {
    return {
      isOpen: false,
      isFocused: false,
      isExpanded: false,
      isSearching: false,
      query: '',
      results: []
    };
  }

  reduce(state, action) {
    switch (action.type) {
      case ActionTypes.SEARCH_SHOW:
        return {
          ...state,
          isOpen: true,
          isExpanded: false
        };
      case ActionTypes.SEARCH_HIDE:
        return this.getInitialState();
      case ActionTypes.SEARCH_TOGGLE_FOCUS:
        return {
          ...state,
          isFocused: action.isEnable
        };
      case ActionTypes.SEARCH_TOGGLE_EXPAND:
        return {
          ...state,
          isExpanded: !state.isExpanded
        };
      case ActionTypes.SEARCH_TEXT:
        return {
          ...state,
          query: action.query,
          isSearching: true
        };
      case ActionTypes.SEARCH_TEXT_SUCCESS:
        return {
          ...state,
          results: action.query ? action.response : [],
          isSearching: false
        };
      case ActionTypes.SEARCH_TEXT_ERROR:
        console.log(action);
        return state;
      default:
        return state;
    }
  }

  isOpen() {
    return this.getState().isOpen;
  }

  isSearching() {
    return this.getState().isSearching;
  }

  isExpanded() {
    return this.getState().isExpanded;
  }

  getQuery() {
    return this.getState().query;
  }

  getAllResults() {
    return this.getState().results;
  }
}

export default new SearchMessagesStore(Dispatcher);
