/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

import { EventEmitter } from 'events';
import { register } from 'dispatcher/ActorAppDispatcher';
import { ActionTypes, PeerTypes, AsyncActionStates } from 'constants/ActorAppConstants';
import ActorClient from 'utils/ActorClient';
import { hasMember } from 'utils/GroupUtils';

const CHANGE_EVENT = 'change';

let _group = null,
    _kickUserState = [];

class KickUserStore extends EventEmitter {
  getKickUserState(uid) {
    return hasMember(_group.id, uid)
      ? (_kickUserState[uid] || AsyncActionStates.PENDING)
      : AsyncActionStates.PENDING;
    //return (_kickUserState[uid] || AsyncActionStates.PENDING)
  }

  resetKickUserState(uid) {
    delete _kickUserState[uid];
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
      //console.debug('KICK_USER _kickUserState', action.uid, _kickUserState[action.uid]);
      KickUserStoreInstance.emitChange();
      break;
    case ActionTypes.KICK_USER_SUCCESS:
      _kickUserState[action.uid] = AsyncActionStates.SUCCESS;
      //console.debug('KICK_USER_SUCCESS _kickUserState', action.uid, _kickUserState[action.uid]);
      KickUserStoreInstance.emitChange();
      break;
    case ActionTypes.KICK_USER_ERROR:
      _kickUserState[action.uid] = AsyncActionStates.FAILURE;
      //console.debug('KICK_USER_ERROR _kickUserState', action.uid, _kickUserState[action.uid]);
      KickUserStoreInstance.emitChange();
      break;
  }
});

export default KickUserStoreInstance;
