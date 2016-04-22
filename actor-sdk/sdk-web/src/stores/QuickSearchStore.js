/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

import { ReduceStore } from 'flux/utils';
import Dispatcher from '../dispatcher/ActorAppDispatcher';
import { ActionTypes } from '../constants/ActorAppConstants';

class QuickSearchStore extends ReduceStore {
  getInitialState() {
    return {
      list: []
    }
  }

  reduce(state, action) {
    switch (action.type) {
      case ActionTypes.QUICK_SEARCH_CHANGED:
        return {
          ...state,
          list: action.list
        }
      default:
        return state;
    }
  }

  getList() {
    return this.getState().list
  }
}

export default new QuickSearchStore(Dispatcher);
