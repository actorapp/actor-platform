import { EventEmitter } from 'events';
import ActorAppDispatcher from 'dispatcher/ActorAppDispatcher';
import { ActionTypes } from 'constants/ActorAppConstants';

const CHANGE_EVENT = 'change';

let _isModalOpen = false;

class AppCacheStore extends EventEmitter {
  constructor() {
    super();
  }

  isModalOpen() {
    return _isModalOpen;
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

let AppCacheStoreInstance = new AppCacheStore();

AppCacheStoreInstance.dispatchToken = ActorAppDispatcher.register(action => {
  switch(action.type) {
    case ActionTypes.APP_UPDATE_MODAL_SHOW:
      _isModalOpen = true;
      AppCacheStoreInstance.emitChange();
      break;
    case ActionTypes.APP_UPDATE_MODAL_HIDE:
      _isModalOpen = false;
      AppCacheStoreInstance.emitChange();
      break;
    default:
      return;
  }
});

export default AppCacheStoreInstance;
