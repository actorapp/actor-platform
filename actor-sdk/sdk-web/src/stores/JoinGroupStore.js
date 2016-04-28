/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

import { ReduceStore } from 'flux/utils';
import Dispatcher from '../dispatcher/ActorAppDispatcher';
import { ActionTypes, AsyncActionStates } from '../constants/ActorAppConstants';

class JoinGroupStore extends ReduceStore {
  getInitialState() {
    return {
      token: null,
      status: AsyncActionStates.PENDING,
      error: null
    };
  }

  reduce(state, action) {
    switch (action.type) {
      case ActionTypes.GROUP_JOIN_VIA_LINK:
        return {
          ...state,
          token: action.token,
          status: AsyncActionStates.PROCESSING
        };

      case ActionTypes.GROUP_JOIN_VIA_LINK_SUCCESS:
        return {
          ...state,
          status: AsyncActionStates.SUCCESS
        };

      case ActionTypes.GROUP_JOIN_VIA_LINK_ERROR:
        return {
          ...state,
          status: AsyncActionStates.FAILURE,
          error: action.error
        };

      default:
        return state;
    }
  }
}

export default new JoinGroupStore(Dispatcher);
