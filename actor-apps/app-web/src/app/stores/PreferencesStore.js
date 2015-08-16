import { EventEmitter } from 'events';
import ActorAppDispatcher from 'dispatcher/ActorAppDispatcher';
import { ActionTypes } from 'constants/ActorAppConstants';

const CHANGE_EVENT = 'change';

let _isModalOpen = false;

class SettingsStore extends EventEmitter {
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

let SettingsStoreInstance = new SettingsStore();

SettingsStoreInstance.dispatchToken = ActorAppDispatcher.register(action => {
  //console.info(action);
  switch(action.type) {
    case ActionTypes.SETTINGS_SHOW:
      _isModalOpen = true;
      break;
    case ActionTypes.SETTINGS_HIDE:
      _isModalOpen = false;
      break;
    default:
      return;
  }
  SettingsStoreInstance.emitChange();
});

export default SettingsStoreInstance;
