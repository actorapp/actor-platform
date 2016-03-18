/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

import { Store } from 'flux/utils';
import Dispatcher from '../dispatcher/ActorAppDispatcher';
import { ActionTypes, AsyncActionStates } from '../constants/ActorAppConstants';

class KickUserStore extends Store {
  constructor(dispatcher) {
    super(dispatcher);

    this._kickUserState = {};
  }

  getKickUserState(uid) {
    return this._kickUserState[uid] || AsyncActionStates.PENDING;
  }

  __onDispatch(action) {
    switch(action.type) {
      case ActionTypes.KICK_USER:
        this._kickUserState[action.uid] = AsyncActionStates.PROCESSING;
        this.__emitChange();
        break;
      case ActionTypes.KICK_USER_SUCCESS:
        delete this._kickUserState[action.uid];
        this.__emitChange();
        break;
      case ActionTypes.KICK_USER_ERROR:
        this._kickUserState[action.uid] = AsyncActionStates.FAILURE;
        this.__emitChange();
        break;
      default:
    }
  }
}

export default new KickUserStore(Dispatcher);
