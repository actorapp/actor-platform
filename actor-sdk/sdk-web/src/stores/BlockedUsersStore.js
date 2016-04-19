/*
 * Copyright (C) 2015-2016 Actor LLC. <https://actor.im>
 */

import { ReduceStore } from 'flux/utils';
import Dispatcher from '../dispatcher/ActorAppDispatcher';
import { ActionTypes } from '../constants/ActorAppConstants';

class BlockedUsersStore extends ReduceStore {
  getInitialState() {
    return {
      isOpen: false,
      users: [],
      query: null
    };
  }

  reduce(state, action) {
    switch (action.type) {
      case ActionTypes.BLOCKED_USERS_OPEN:
        return {
          ...state,
          isOpen: true
        };

      case ActionTypes.BLOCKED_USERS_HIDE:
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
