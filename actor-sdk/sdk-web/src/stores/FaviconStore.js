/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

import { Store } from 'flux/utils';
import Dispatcher from '../dispatcher/ActorAppDispatcher';
import { ActionTypes } from '../constants/ActorAppConstants';

const FaviconPath = {
  DEFAULT: 'assets/images/favicon.png',
  NOTIFICATION: 'assets/images/favicon_notification.png'
};

let _iconPath = FaviconPath.DEFAULT;

class FaviconStore extends Store {
  constructor(dispatcher) {
    super(dispatcher);
  }

  getFaviconPath() {
    return _iconPath;
  }

  __onDispatch(action) {
    switch(action.type) {
      case ActionTypes.FAVICON_SET:
        if (action.counter === 0) {
          _iconPath = FaviconPath.DEFAULT;
        } else {
          _iconPath = FaviconPath.NOTIFICATION;
        }
        this.__emitChange();
        break;
      default:
    }
  };
}

export default new FaviconStore(Dispatcher);
