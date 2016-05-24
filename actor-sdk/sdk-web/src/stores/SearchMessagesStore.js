/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

import { ReduceStore } from 'flux/utils';
import Dispatcher from '../dispatcher/ActorAppDispatcher';
import { ActionTypes } from '../constants/ActorAppConstants';

class SearchMessagesStore extends ReduceStore {
  getInitialState() {
    return {
      query: '',
      results: [],
      error: null,
      isOpen: false,
      isSearching: false
    };
  }

  reduce(state, action) {
    switch (action.type) {
      case ActionTypes.SEARCH_MESSAGES_SHOW:
        return {
          ...state,
          isOpen: true
        };

      case ActionTypes.BIND_DIALOG_PEER:
      case ActionTypes.SEARCH_MESSAGES_HIDE:
        return this.getInitialState();

      case ActionTypes.SEARCH_MESSAGES_SET_QUERY:
        return {
          ...state,
          query: action.query,
          isOpen: true,
          isSearching: true
        };

      case ActionTypes.SEARCH_TEXT_SUCCESS:
        return {
          ...state,
          results: action.query ? action.response : [],
          error: null,
          isSearching: false
        };

      case ActionTypes.SEARCH_TEXT_ERROR:
        return {
          ...state,
          error: action.error,
          isSearching: false
        };

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
