import { EventEmitter } from 'events';
import ActorAppDispatcher from 'dispatcher/ActorAppDispatcher';
import { ActionTypes } from 'constants/ActorAppConstants';

import ActorClient from 'utils/ActorClient';

const CHANGE_EVENT = 'change';

const FaviconPath = {
  DEFAULT: 'assets/img/favicon_logo_196x196.png',
  NOTIFICATION: 'assets/img/favicon_notification.png'
};

let _iconPath = FaviconPath.DEFAULT;

class FaviconStore extends EventEmitter {
  getFaviconPath() {
    return _iconPath;
  }

  emitChange() {
    this.emit(CHANGE_EVENT);
  }

  addChangeListener(callback) {
    this.on(CHANGE_EVENT, callback);
  }

  removeChangeListener(callback) {
    this.removeListener(CHANGE_EVENT, callback);
  }
}

let FaviconStoreInstance = new FaviconStore();

let lastCounter = 0;

const onSetLoggedIn = () => {
  ActorClient.bindTempGlobalCounter((c) => {
    if (c.counter == 0) {
      _iconPath = FaviconPath.DEFAULT;
    } else {
      _iconPath = FaviconPath.NOTIFICATION;
    }

    FaviconStoreInstance.emitChange();
  });
};

FaviconStoreInstance.dispatchToken = ActorAppDispatcher.register(action => {
  switch(action.type) {
    case ActionTypes.SET_LOGGED_IN:
      onSetLoggedIn();
    case ActionTypes.FAVICON_SET_DEFAULT:
      _iconPath = FaviconPath.DEFAULT;
      FaviconStoreInstance.emitChange();
      break;
    case ActionTypes.FAVICON_SET_NOTIFICATION:
      _iconPath = FaviconPath.NOTIFICATION;
      FaviconStoreInstance.emitChange();
      break;
    default:
      return;
  }
});

export default FaviconStoreInstance;
