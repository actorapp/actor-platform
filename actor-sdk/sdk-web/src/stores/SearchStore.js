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
      results: {
        contacts: [],
        groups: []
      }
    };
  }

  reduce(state, action) {
    switch (action.type) {
      case ActionTypes.SEARCH:
        console.debug(action);
        return {
          ...state,
          query: action.query,
          results: action.results
        }
        break;
      default:
        return state;
    }
  }
}

export default new SearchStore(Dispatcher);
