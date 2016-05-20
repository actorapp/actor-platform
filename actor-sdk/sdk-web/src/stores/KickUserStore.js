/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

import { Store } from 'flux/utils';
import Dispatcher from '../dispatcher/ActorAppDispatcher';
import { ActionTypes, AsyncActionStates } from '../constants/ActorAppConstants';

class KickUserStore extends Store {
  constructor(dispatcher) {
    super(dispatcher);

    this.kickUserState = {};
  }

  getKickUserState(uid) {
    return (this.kickUserState[uid] || AsyncActionStates.PENDING);
  }

  resetKickUserState(uid) {
    delete this.kickUserState[uid];
  }

  __onDispatch(action) {
    switch(action.type) {
      case ActionTypes.KICK_USER:
        this.kickUserState[action.uid] = AsyncActionStates.PROCESSING;
        this.__emitChange();
        break;
      case ActionTypes.KICK_USER_SUCCESS:
        this.resetKickUserState(action.uid);
        this.__emitChange();
        break;
      case ActionTypes.KICK_USER_ERROR:
        this.kickUserState[action.uid] = AsyncActionStates.FAILURE;
        this.__emitChange();
        break;
      default:
    }
  }
}

export default new KickUserStore(Dispatcher);
