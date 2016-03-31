/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

import { Store } from 'flux/utils';
import Dispatcher from '../dispatcher/ActorAppDispatcher';
import { ActionTypes } from '../constants/ActorAppConstants';
import ActorClient from '../utils/ActorClient';

class NotificationsStore extends Store {
  isNotificationsEnabled(peer) {
    return ActorClient.isNotificationsEnabled(peer);
  }

  __onDispatch(action) {
    switch(action.type) {
      case ActionTypes.NOTIFICATION_CHANGE:
        this.__emitChange();
        break;
      default:
    }
  }
}

export default new NotificationsStore(Dispatcher);
