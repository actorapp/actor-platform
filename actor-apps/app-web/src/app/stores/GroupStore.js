/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

import { EventEmitter } from 'events';
import ActorClient from 'utils/ActorClient';

import DialogStore from 'stores/DialogStore'

import { register } from 'dispatcher/ActorAppDispatcher';
import { ActionTypes, AsyncActionStates } from 'constants/ActorAppConstants';

const CHANGE_EVENT = 'change';

let _integrationToken = null;

class GroupStore extends EventEmitter {
  getGroup(gid) {
    return ActorClient.getGroup(gid);
  }

  getIntegrationToken() {
    return _integrationToken;
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

let GroupStoreInstance = new GroupStore();

GroupStoreInstance.dispatchToken = register(action => {
  switch (action.type) {
    case ActionTypes.LEFT_GROUP:
      GroupStoreInstance.emitChange();
      break;

    case ActionTypes.GET_INTEGRATION_TOKEN:
      GroupStoreInstance.emitChange();
      break;
    case ActionTypes.GET_INTEGRATION_TOKEN_SUCCESS:
      _integrationToken = action.response;
      GroupStoreInstance.emitChange();
      break;
    case ActionTypes.GET_INTEGRATION_TOKEN_ERROR:
      _integrationToken = null;
      GroupStoreInstance.emitChange();
      break;
  }
});

export default GroupStoreInstance;
