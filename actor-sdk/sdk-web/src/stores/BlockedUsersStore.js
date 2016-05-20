/*
 * Copyright (C) 2015-2016 Actor LLC. <https://actor.im>
 */

import { ReduceStore } from 'flux/utils';
import Dispatcher from '../dispatcher/ActorAppDispatcher';
import { ActionTypes } from '../constants/ActorAppConstants';

class BlockedUsersStore extends ReduceStore {
  getInitialState() {
    return {
      users: [],
      query: null
    };
  }

  reduce(state, action) {
    switch (action.type) {
      case ActionTypes.PREFERENCES_MODAL_HIDE:
        return this.getInitialState();

      case ActionTypes.BLOCKED_USERS_SET:
        return {
          ...state,
          users: action.users
        };

      case ActionTypes.BLOCKED_USERS_SET_QUERY:
        return {
          ...state,
          query: action.query
        };

      default:
        return state;
    }
  }
}

export default new BlockedUsersStore(Dispatcher);
