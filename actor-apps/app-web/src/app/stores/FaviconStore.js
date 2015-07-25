import { EventEmitter } from 'events';
import ActorAppDispatcher from 'dispatcher/ActorAppDispatcher';
import { ActionTypes } from 'constants/ActorAppConstants';

const CHANGE_EVENT = 'change';

const FaviconPath = {
  DEFAULT: 'assets/img/favicon_logo_196x196.png',
  NOTIFICATION: 'assets/img/favicon_notification_512x512.png'
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

FaviconStoreInstance.dispatchToken = ActorAppDispatcher.register(action => {
  switch(action.type) {
    case ActionTypes.FAVICON_SET_DEFAULT:
      _iconPath = FaviconPath.DEFAULT;
      break;
    case ActionTypes.FAVICON_SET_NOTIFICATION:
      _iconPath = FaviconPath.NOTIFICATION;
      break;
    default:
      return;
  }
  FaviconStoreInstance.emitChange();
});

export default FaviconStoreInstance;
