/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

import { ReduceStore } from 'flux/utils';
import Dispatcher from '../dispatcher/ActorAppDispatcher';
import { ActionTypes } from '../constants/ActorAppConstants';

class QuickSearchStore extends ReduceStore {
  getInitialState() {
    return []
  }

  reduce(state, action) {
    switch (action.type) {
      case ActionTypes.QUICK_SEARCH_CHANGED:
        return action.list
      default:
        return state;
    }
  }
}

export default new QuickSearchStore(Dispatcher);
