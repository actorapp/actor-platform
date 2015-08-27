/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

import { EventEmitter } from 'events';
import { register } from 'dispatcher/ActorAppDispatcher';
import { ActionTypes, PeerTypes, AsyncActionStates } from 'constants/ActorAppConstants';

import ActorClient from 'utils/ActorClient';

const CHANGE_EVENT = 'change';

let _group = null,
    _kickUserState = [];

class KickUserStore extends EventEmitter {
  emitChange() {
    this.emit(CHANGE_EVENT);
  }

  addChangeListener(callback) {
    this.on(CHANGE_EVENT, callback);
  }

  removeChangeListener(callback) {
    this.removeListener(CHANGE_EVENT, callback);
  }

  getKickUserState(uid) {
    return (_kickUserState[uid] || AsyncActionStates.PENDING);
  }

  resetKickUserState(uid) {
    delete _kickUserState[uid];
  }
}

let KickUserStoreInstance = new KickUserStore();

KickUserStoreInstance.dispatchToken = register(action => {
  switch (action.type) {
    case ActionTypes.SELECT_DIALOG_PEER:
      if (action.peer.type === PeerTypes.GROUP) {
        _group = ActorClient.getGroup(action.peer.id);
        KickUserStoreInstance.emitChange();
      }
      break;

    case ActionTypes.KICK_USER:
      _kickUserState[action.uid] = AsyncActionStates.PROCESSING;
      KickUserStoreInstance.emitChange();
      break;
    case ActionTypes.KICK_USER_SUCCESS:
      delete _kickUserState[action.uid];
      KickUserStoreInstance.emitChange();
      break;
    case ActionTypes.KICK_USER_ERROR:
      _kickUserState[action.uid] = AsyncActionStates.FAILURE;
      KickUserStoreInstance.emitChange();
      break;
  }
});

export default KickUserStoreInstance;
