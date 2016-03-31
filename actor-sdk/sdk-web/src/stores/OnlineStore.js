/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

import { Store } from 'flux/utils';
import Dispatcher from '../dispatcher/ActorAppDispatcher';
import { ActionTypes } from '../constants/ActorAppConstants';

let message = '',
    online = 0,
    total = 0,
    isOnline = false,
    isNotMember =  false;

class OnlineStore extends Store {
  constructor(dispatcher){
    super(dispatcher);
  }

  getMessage() {
    return message;
  }

  getTotal() {
    return total;
  }

  getOnline() {
    return online;
  }

  isOnline() {
    return isOnline;
  }

  isNotMember() {
    return isNotMember;
  }

  __onDispatch(action) {
    switch(action.type) {
      case ActionTypes.GROUP_ONLINE_CHANGE:
        message = action.message;
        online = action.online;
        total = action.total;
        isNotMember = action.isNotMember;
        this.__emitChange();
        break;
      case ActionTypes.USER_ONLINE_CHANGE:
        message = action.message;
        isOnline = action.isOnline;
        this.__emitChange();
        break;
      default:
    }
  }
}

export default new OnlineStore(Dispatcher);
