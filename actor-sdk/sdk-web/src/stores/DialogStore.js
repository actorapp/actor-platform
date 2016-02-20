/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

import { Store } from 'flux/utils';
import Dispatcher from '../dispatcher/ActorAppDispatcher';
import { ActionTypes, PeerTypes } from '../constants/ActorAppConstants';
import ActorClient from '../utils/ActorClient';

let _currentPeer = null,
    _lastPeer = null;

class DialogStore extends Store {
  getCurrentPeer() {
    return _currentPeer;
  }

  getLastPeer() {
    return _lastPeer;
  }

  isMember() {
    if (_currentPeer !== null && _currentPeer.type === PeerTypes.GROUP) {
      const group = ActorClient.getGroup(_currentPeer.id);
      return group.members.length !== 0;
    } else {
      return true;
    }
  }

  __onDispatch(action) {
    switch(action.type) {
      case ActionTypes.BIND_DIALOG_PEER:
        _lastPeer = _currentPeer;
        _currentPeer = action.peer;
        this.__emitChange();
        break;
      case ActionTypes.UNBIND_DIALOG_PEER:
        _lastPeer = _currentPeer;
        _currentPeer = null;
        this.__emitChange();
        break;
      default:
    }
  }
}

export default new DialogStore(Dispatcher);
