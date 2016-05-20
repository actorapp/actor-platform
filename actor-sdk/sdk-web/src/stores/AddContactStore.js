/*
 * Copyright (C) 2016 Actor LLC. <https://actor.im>
 */

import { ReduceStore } from 'flux/utils';
import Dispatcher from '../dispatcher/ActorAppDispatcher';
import { ActionTypes } from '../constants/ActorAppConstants';

class AddContactStore extends ReduceStore {
  getInitialState() {
    return {
      query: '',
      isSearching: false,
      results: []
    };
  }

  reduce(state, action) {
    switch(action.type) {
      case ActionTypes.CONTACT_ADD_MODAL_HIDE:
        return this.getInitialState();

      case ActionTypes.CONTACT_FIND:
        return {
          ...state,
          query: action.query,
          isSearching: true
        }
      case ActionTypes.CONTACT_FIND_SUCCESS:
        return {
          ...state,
          results: action.response,
          isSearching: false
        }
      case ActionTypes.CONTACT_FIND_ERROR:
        return {
          ...state,
          isSearching: false
        }
      default:
        return state;
    }
  }

  isSearching() {
    return this.getState().isSearching;
  }

  getQuery() {
    return this.getState().query;
  }

  getResults() {
    return this.getState().results;
  }
}

export default new AddContactStore(Dispatcher);
