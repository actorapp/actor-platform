/*
 * Copyright (C) 2016 Actor LLC. <https://actor.im>
 */

import { ReduceStore } from 'flux/utils';
import Dispatcher from '../dispatcher/ActorAppDispatcher';
import { ActionTypes } from '../constants/ActorAppConstants';

class GroupListStore extends ReduceStore {
  getInitialState() {
    return []
  }

  reduce(state, action) {
    switch (action.type) {
      case ActionTypes.GROUP_LIST_LOAD_SUCCESS:
        return action.response;
      case ActionTypes.GROUP_LIST_LOAD_ERROR:
        console.error(action.error);
        return state;
      default:
        return state;
    }
  }
}

export default new GroupListStore(Dispatcher);
