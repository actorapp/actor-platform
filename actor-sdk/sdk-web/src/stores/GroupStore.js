/*
 * Copyright (C) 2016 Actor LLC. <https://actor.im>
 */

import { ReduceStore } from 'flux/utils';
import Dispatcher from '../dispatcher/ActorAppDispatcher';
import { ActionTypes } from '../constants/ActorAppConstants';
import ActorClient from '../utils/ActorClient';

class GroupStore extends ReduceStore {
  getInitialState() {
    return {
      token: null
    };
  }

  reduce(state, action) {
    switch (action.type) {

      case ActionTypes.GROUP_GET_TOKEN:
        return state;
      case ActionTypes.GROUP_GET_TOKEN_SUCCESS:
        return {
          ...state,
          token: action.response
        }
      case ActionTypes.GROUP_GET_TOKEN_ERROR:
        return this.getInitialState()

      case ActionTypes.GROUP_CLEAR:
      case ActionTypes.GROUP_CLEAR_SUCCESS:
      case ActionTypes.GROUP_CLEAR_ERROR:
      case ActionTypes.GROUP_LEAVE:
      case ActionTypes.GROUP_LEAVE_SUCCESS:
      case ActionTypes.GROUP_LEAVE_ERROR:
      case ActionTypes.GROUP_DELETE:
      case ActionTypes.GROUP_DELETE_SUCCESS:
      case ActionTypes.GROUP_DELETE_ERROR:
      default:
        return state;
    }
  }

  /**
   * Get group information
   *
   * @param gid {number} Group id
   * @returns {object} Group information
   */
  getGroup(gid) {
    return ActorClient.getGroup(gid);
  }

  /**
   * Get group integration token
   *
   * @returns {string|null}
   */
  getToken() {
    return this.getState().token;
  }
}

export default new GroupStore(Dispatcher);
