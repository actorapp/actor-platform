/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

import { Store } from 'flux/utils';
import Dispatcher from '../dispatcher/ActorAppDispatcher';
import { ActionTypes, AsyncActionStates } from '../constants/ActorAppConstants';
import ActorClient from '../utils/ActorClient';

let _integrationToken = null;

class GroupStore extends Store {
  constructor(dispatcher) {
    super(dispatcher);
  }

  getGroup(gid) {
    return ActorClient.getGroup(gid);
  }

  getToken() {
    return _integrationToken;
  }

  __onDispatch = (action) => {
    switch (action.type) {

      case ActionTypes.GROUP_GET_TOKEN:
        this.__emitChange();
        break;
      case ActionTypes.GROUP_GET_TOKEN_SUCCESS:
        _integrationToken = action.response;
        this.__emitChange();
        break;
      case ActionTypes.GROUP_GET_TOKEN_ERROR:
        _integrationToken = null;
        this.__emitChange();
        break;

      case ActionTypes.GROUP_CLEAR:
      case ActionTypes.GROUP_CLEAR_SUCCESS:
      case ActionTypes.GROUP_CLEAR_ERROR:

      case ActionTypes.GROUP_LEAVE:
      case ActionTypes.GROUP_LEAVE_SUCCESS:
      case ActionTypes.GROUP_LEAVE_ERROR:

      case ActionTypes.GROUP_DELETE:
      case ActionTypes.GROUP_DELETE_SUCCESS:
      case ActionTypes.GROUP_DELETE_ERROR:
        this.__emitChange();
        break;
    }
  };
}

export default new GroupStore(Dispatcher);
