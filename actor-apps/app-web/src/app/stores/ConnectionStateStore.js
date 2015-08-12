import { EventEmitter } from 'events';
import ActorAppDispatcher from 'dispatcher/ActorAppDispatcher';
import { ActionTypes } from 'constants/ActorAppConstants';

import ActorClient from 'utils/ActorClient';

const CHANGE_EVENT = 'change';

let _state = '';

class ConnectionStateStore extends EventEmitter {
  emitChange() {
    this.emit(CHANGE_EVENT);
  }

  addChangeListener(callback) {
    this.on(CHANGE_EVENT, callback);
  }

  removeChangeListener(callback) {
    this.removeListener(CHANGE_EVENT, callback);
  }

  getState() {
    return _state;
  }

  onStateChange = (state) => {
    _state = state;
    this.emitChange();
  }
}

let ConnectionStateStoreInstance = new ConnectionStateStore();

ConnectionStateStoreInstance.dispatchToken = ActorAppDispatcher.register(action => {
  switch(action.type) {
    case ActionTypes.APP_VISIBLE:
      ActorClient.bindConnectState(ConnectionStateStoreInstance.onStateChange);
      break;
    case ActionTypes.APP_HIDDEN:
      ActorClient.unbindConnectState(ConnectionStateStoreInstance.onStateChange);
      break;
    default:
      return;
  }
});

export default ConnectionStateStoreInstance;
